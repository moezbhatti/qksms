/*
 * Copyright 2013 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.send_message

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.os.bundleOf
import com.android.mms.MmsConfig
import com.android.mms.dom.smil.parser.SmilXmlSerializer
import com.android.mms.util.DownloadManager
import com.android.mms.util.RateController
import com.google.android.mms.ContentType
import com.google.android.mms.InvalidHeaderValueException
import com.google.android.mms.MMSPart
import com.google.android.mms.pdu_alt.CharacterSets
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduBody
import com.google.android.mms.pdu_alt.PduComposer
import com.google.android.mms.pdu_alt.PduHeaders
import com.google.android.mms.pdu_alt.PduPart
import com.google.android.mms.pdu_alt.PduPersister
import com.google.android.mms.pdu_alt.SendReq
import com.google.android.mms.smil.SmilHelper
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Class to process transaction requests for sending
 *
 * @author Jake Klinker
 */
class Transaction @JvmOverloads constructor(private val context: Context, settings: Settings = Settings()) {

    companion object {
        var settings: Settings = Settings()

        const val MMS_SENT = "com.moez.QKSMS.MMS_SENT"
        const val EXTRA_CONTENT_URI = "content_uri"
        const val EXTRA_FILE_PATH = "file_path"

        const val NOTIFY_SMS_FAILURE = "com.moez.QKSMS.NOTIFY_SMS_FAILURE"
        const val MMS_UPDATED = "com.moez.QKSMS.MMS_UPDATED"
        const val MMS_ERROR = "com.klinker.android.send_message.MMS_ERROR"
        const val REFRESH = "com.klinker.android.send_message.REFRESH"
        const val MMS_PROGRESS = "com.klinker.android.send_message.MMS_PROGRESS"
        const val NOTIFY_OF_DELIVERY = "com.klinker.android.send_message.NOTIFY_DELIVERY"
        const val NOTIFY_OF_MMS = "com.klinker.android.messaging.NEW_MMS_DOWNLOADED"


        const val DEFAULT_EXPIRY_TIME = (7 * 24 * 60 * 60).toLong()
        const val DEFAULT_PRIORITY = PduHeaders.PRIORITY_NORMAL
    }

    init {
        Transaction.settings = settings
    }

    /**
     * Called to send a new message depending on settings and provided Message object
     * If you want to send message as mms, call this from the UI thread
     *
     * @param threadId is the thread id of who to send the message to (can also be set to Transaction.NO_THREAD_ID)
     */
    fun sendNewMessage(subId: Int, threadId: Long, addresses: List<String>, parts: List<MMSPart>, subject: String?) {

        RateController.init(context)
        DownloadManager.init(context)

        Timber.v("sending mms")
        try {
            val fileName = "send." + Math.abs(Random().nextLong()).toString() + ".dat"
            val sendFile = File(context.cacheDir, fileName)

            val sendReq = buildPdu(context, addresses, subject, parts)
            val persister = PduPersister.getPduPersister(context)
            val messageUri = persister.persist(sendReq, Uri.parse("content://mms/outbox"), true, true, null)

            val sentIntent = Intent(MMS_SENT)
            BroadcastUtils.addClassName(context, sentIntent, MMS_SENT)

            sentIntent.putExtra(EXTRA_CONTENT_URI, messageUri.toString())
            sentIntent.putExtra(EXTRA_FILE_PATH, sendFile.path)
            val sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val updatedIntent = Intent(MMS_UPDATED).putExtra("uri", messageUri.toString())
            BroadcastUtils.addClassName(context, updatedIntent, MMS_UPDATED)
            context.sendBroadcast(updatedIntent)

            val contentUri: Uri? = try {
                FileOutputStream(sendFile).use { writer ->
                    writer.write(PduComposer(context, sendReq).make())
                    Uri.Builder()
                            .authority(context.packageName + ".MmsFileProvider")
                            .path(fileName)
                            .scheme(ContentResolver.SCHEME_CONTENT)
                            .build()
                }
            } catch (e: IOException) {
                Timber.e(e, "Error writing send file")
                null
            }

            val configOverrides = bundleOf(
                    Pair(SmsManager.MMS_CONFIG_GROUP_MMS_ENABLED, true),
                    Pair(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE, MmsConfig.getMaxMessageSize()))

            MmsConfig.getHttpParams()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { configOverrides.putString(SmsManager.MMS_CONFIG_HTTP_PARAMS, it) }

            if (contentUri != null) {
                SmsManagerFactory.createSmsManager(subId).sendMultimediaMessage(context, contentUri, null, configOverrides, sentPI)
            } else {
                Timber.e("Error writing sending Mms")
                try {
                    sentPI.send(SmsManager.MMS_ERROR_IO_ERROR)
                } catch (e: PendingIntent.CanceledException) {
                    Timber.e(e)
                }

            }
        } catch (e: Exception) {
            Timber.e(e, "Error using system sending method")
        }

    }

    private fun buildPdu(context: Context, recipients: List<String>, subject: String?, parts: List<MMSPart>): SendReq {
        val req = SendReq()

        Utils.getMyPhoneNumber(context)?.takeIf(String::isNotEmpty)?.let(::EncodedStringValue)?.let(req::setFrom) // From
        recipients.map(::EncodedStringValue).forEach(req::addTo) // To
        subject?.takeIf(String::isNotEmpty)?.let(::EncodedStringValue)?.let(req::setSubject) // Subject

        req.date = System.currentTimeMillis() / 1000
        req.body = PduBody()

        // Parts
        parts.map(this::partToPduPart).forEach { req.body.addPart(it) }

        // SMIL document for compatibility
        req.body.addPart(0, PduPart().apply {
            contentId = "smil".toByteArray()
            contentLocation = "smil.xml".toByteArray()
            contentType = ContentType.APP_SMIL.toByteArray()
            data = ByteArrayOutputStream()
                    .apply { SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(req.body), this) }
                    .toByteArray()
        })

        req.messageSize = parts.mapNotNull { it.data?.size }.sum().toLong()
        req.messageClass = PduHeaders.MESSAGE_CLASS_PERSONAL_STR.toByteArray()
        req.expiry = DEFAULT_EXPIRY_TIME

        try {
            req.priority = DEFAULT_PRIORITY
            req.deliveryReport = PduHeaders.VALUE_NO
            req.readReport = PduHeaders.VALUE_NO
        } catch (e: InvalidHeaderValueException) {
            Timber.w(e)
        }

        return req
    }

    private fun partToPduPart(part: MMSPart): PduPart = PduPart().apply {
        val filename = part.name

        // Set Charset if it's a text media.
        if (part.mimeType.startsWith("text")) {
            charset = CharacterSets.UTF_8
        }

        // Set Content-Type.
        contentType = part.mimeType.toByteArray()

        // Set Content-Location.
        contentLocation = filename.toByteArray()
        val index = filename.lastIndexOf(".")
        contentId = (if (index == -1) filename else filename.substring(0, index)).toByteArray()
        data = part.data
    }

}