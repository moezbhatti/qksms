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
package com.moez.QKSMS.domain.interactor

import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.util.NotificationManager
import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class ReceiveMessage @Inject constructor(
        private val context: Context,
        private val messageRepo: MessageRepository,
        private val notificationManager: NotificationManager)
    : Interactor<ReceiveMessage.Params, Unit>() {

    data class Params(val address: String, val body: String, val sentTime: Long)

    override fun buildObservable(params: Params): Flowable<Unit> {
        val values = ContentValues()
        values.put(Telephony.Sms.ADDRESS, params.address)
        values.put(Telephony.Sms.BODY, params.body)
        values.put(Telephony.Sms.DATE_SENT, params.sentTime)
        values.put(Telephony.Sms.READ, false)
        values.put(Telephony.Sms.SEEN, false)

        val contentResolver = context.contentResolver
        return Flowable.just(values)
                .map { contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values) }
                .doOnNext { uri -> messageRepo.addMessageFromUri(uri) }
                .doOnNext { notificationManager.update(messageRepo) }
                .map { Unit }
    }

}