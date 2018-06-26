/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import com.google.android.mms.MmsException
import com.google.android.mms.util_alt.SqliteWrapper
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.interactor.SyncMessage
import dagger.android.AndroidInjection
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MmsSentReceiver : BroadcastReceiver() {

    @Inject lateinit var syncMessage: SyncMessage

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        Timber.v("MMS sending result: $resultCode")
        val uri = Uri.parse(intent.getStringExtra(Transaction.EXTRA_CONTENT_URI))
        Timber.v(uri.toString())

        when (resultCode) {
            Activity.RESULT_OK -> {
                Timber.v("MMS has finished sending, marking it as so in the database")
                val values = ContentValues(1)
                values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT)
                SqliteWrapper.update(context, context.contentResolver, uri, values, null, null)
            }

            else -> {
                Timber.v("MMS has failed to send, marking it as so in the database")
                try {
                    val messageId = ContentUris.parseId(uri)

                    val values = ContentValues(1)
                    values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_FAILED)
                    SqliteWrapper.update(context, context.contentResolver, Telephony.Mms.CONTENT_URI, values,
                            "${Telephony.Mms._ID} = ?", arrayOf(messageId.toString()))

                    // TODO this query isn't able to find any results
                    // Need to figure out why the message isn't appearing in the PendingMessages Uri,
                    // so that we can properly assign the error type
                    val errorTypeValues = ContentValues(1)
                    errorTypeValues.put(Telephony.MmsSms.PendingMessages.ERROR_TYPE, Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT)
                    SqliteWrapper.update(context, context.contentResolver, Telephony.MmsSms.PendingMessages.CONTENT_URI,
                            errorTypeValues, "${Telephony.MmsSms.PendingMessages.MSG_ID} = ?", arrayOf(messageId.toString()))

                } catch (e: MmsException) {
                    e.printStackTrace()
                }
            }
        }

        val filePath = intent.getStringExtra(Transaction.EXTRA_FILE_PATH)
        Timber.v(filePath)
        File(filePath).delete()

        Uri.parse(intent.getStringExtra("content_uri"))?.let { uri ->
            val pendingResult = goAsync()
            syncMessage.execute(uri) { pendingResult.finish() }
        }
    }

}