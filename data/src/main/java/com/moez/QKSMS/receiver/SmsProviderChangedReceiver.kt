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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moez.QKSMS.interactor.SyncMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * When the SMS contentprovider is changed by a process other than us, we need to sync the Uri that
 * was changed.
 *
 * This can happen if a message is sent through something like Pushbullet or Google Assistant.
 *
 * This only works on API 24+, so to fully solve this problem we'll need a smarter way of running
 * partial syncs on older devices.
 *
 * https://developer.android.com/reference/android/provider/Telephony.Sms.Intents.html#ACTION_EXTERNAL_PROVIDER_CHANGE
 */
class SmsProviderChangedReceiver : BroadcastReceiver() {

    @Inject lateinit var syncMessage: SyncMessage

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        // Obtain the uri for the changed data
        // If the value is null, don't continue
        val uri = intent.data ?: return

        // Sync the message to our realm
        val pendingResult = goAsync()
        syncMessage.execute(uri) { pendingResult.finish() }
    }

}