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
package com.moez.QKSMS.service

import android.app.IntentService
import android.content.Intent
import com.moez.QKSMS.interactor.SendScheduledMessage
import com.moez.QKSMS.repository.MessageRepository
import dagger.android.AndroidInjection
import javax.inject.Inject

class SendScheduledMessageService : IntentService(null) {

    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var sendScheduledMessage: SendScheduledMessage

    override fun onHandleIntent(intent: Intent) {
        AndroidInjection.inject(this)

        intent.getLongExtra("id", -1L).takeIf { it >= 0 }?.let { id ->
            sendScheduledMessage.buildObservable(id).blockingSubscribe()
        }
    }

}