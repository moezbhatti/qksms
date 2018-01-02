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
package receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import common.di.appComponent
import interactor.ReceiveMessage
import timber.log.Timber
import javax.inject.Inject

class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var receiveMessage: ReceiveMessage

    override fun onReceive(context: Context, intent: Intent) {
        Timber.v("Received SMS: $intent")
        appComponent.inject(this)

        val messages = Sms.Intents.getMessagesFromIntent(intent)
        messages?.takeIf { it.isNotEmpty() }?.let { messages ->
            val address = messages[0].displayOriginatingAddress
            val time = messages[0].timestampMillis
            val body: String = messages
                    .map { message -> message.displayMessageBody }
                    .reduce { body, new -> body + new }

            val pendingResult = goAsync()
            receiveMessage.execute(ReceiveMessage.Params(address, body, time), { pendingResult.finish() })
        }
    }

}