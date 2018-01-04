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
package presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.google.android.mms.ContentType
import com.google.android.mms.pdu.GenericPdu
import com.google.android.mms.pdu.NotificationInd
import com.google.android.mms.pdu.PduHeaders
import com.google.android.mms.pdu.PduParser
import timber.log.Timber

class MmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent.action && ContentType.MMS_MESSAGE == intent.type) {
            val data = intent.getByteArrayExtra("data")
            val parser = PduParser(data)
            var pdu: GenericPdu? = null

            try {
                pdu = parser.parse()
            } catch (e: RuntimeException) {
                Timber.e("Invalid MMS WAP push", e)
            }

            if (pdu == null) {
                Timber.e("Invalid WAP push data")
                return
            }

            when (pdu.messageType) {
                PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND -> {
                    val notificationInd = pdu as NotificationInd
                    val location = String(notificationInd.contentLocation)
                    Timber.v("Received MMS notification: " + location)
                }

                PduHeaders.MESSAGE_TYPE_DELIVERY_IND -> {
                    Timber.v("Received delivery report")
                }

                PduHeaders.MESSAGE_TYPE_READ_ORIG_IND -> {
                    Timber.v("Received read report")
                }
            }
        }
    }

}