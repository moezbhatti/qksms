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
import android.net.Uri
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.data.repository.MessageRepository
import javax.inject.Inject

class MessageDeliveredReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        val uri = Uri.parse(intent.getStringExtra("uri"))
        appComponent.inject(this)

        when (resultCode) {
        // TODO notify about delivery
            Activity.RESULT_OK -> messageRepo.markDelivered(uri)

        // TODO notify about delivery failure
            Activity.RESULT_CANCELED -> messageRepo.markDeliveryFailed(uri, resultCode)
        }
    }

}