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
package interactor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.mlsdev.rximagepicker.RxImageConverters
import common.util.Preferences
import data.repository.MessageRepository
import io.reactivex.Flowable
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class SendMessage @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val messageRepo: MessageRepository
) : Interactor<SendMessage.Params>() {

    data class Params(val threadId: Long, val addresses: List<String>, val body: String, val attachments: List<Uri> = listOf())

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
                .filter { params.addresses.isNotEmpty() }
                .doOnNext {
                    if (params.addresses.size == 1 && params.attachments.isEmpty()) {
                        messageRepo.sendSmsAndPersist(params.threadId, params.addresses.first(), params.body)
                    } else {
                        sendMms(params.threadId, params.addresses, params.body, params.attachments)
                    }
                }
                .doOnNext { messageRepo.markUnarchived(params.threadId) }
    }

    private fun sendMms(threadId: Long, addresses: List<String>, body: String, attachments: List<Uri>) {
        val settings = Settings()
        val message = Message(body, addresses.toTypedArray())

        val bitmaps = attachments.map { uri -> RxImageConverters.uriToBitmap(context, uri).blockingFirst() }
        val totalBytes = bitmaps.sumBy { it.allocationByteCount }

        bitmaps
                .map { bitmap ->
                    val byteRatio = bitmap.allocationByteCount / totalBytes.toFloat()
                    shrink(bitmap, (prefs.mmsSize.get() * 1024 * byteRatio).toInt())
                }
                .forEach { bitmap -> message.addMedia(bitmap, "image/jpeg") }

        val transaction = Transaction(context, settings)
        transaction.sendNewMessage(message, threadId)
    }

    private fun shrink(src: Bitmap, maxBytes: Int): ByteArray {
        var step = 0.0
        val factor = 0.5
        val quality = 60

        val height = src.height
        val width = src.width

        val stream = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        while (maxBytes > 0 && stream.size() > maxBytes) {
            step++
            val scale = Math.pow(factor, step)

            stream.reset()
            Bitmap.createScaledBitmap(src, (width * scale).toInt(), (height * scale).toInt(), false)
                    .compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        return stream.toByteArray()
    }

}