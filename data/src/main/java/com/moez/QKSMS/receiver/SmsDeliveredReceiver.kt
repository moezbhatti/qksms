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
import android.content.Context
import android.content.Intent
import com.moez.QKSMS.interactor.MarkDelivered
import com.moez.QKSMS.interactor.MarkDeliveryFailed
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsDeliveredReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION = "com.moez.QKSMS.SMS_DELIVERED"
    }

    @Inject lateinit var markDelivered: MarkDelivered
    @Inject lateinit var markDeliveryFailed: MarkDeliveryFailed

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        context.unregisterReceiver(this)

        val id = intent.getLongExtra("id", 0L)

        when (resultCode) {
        // TODO notify about delivery
            Activity.RESULT_OK -> {
                val pendingResult = goAsync()
                markDelivered.execute(id) { pendingResult.finish() }
            }

        // TODO notify about delivery failure
            Activity.RESULT_CANCELED -> {
                val pendingResult = goAsync()
                markDeliveryFailed.execute(MarkDeliveryFailed.Params(id, resultCode)) { pendingResult.finish() }
            }
        }
    }

}