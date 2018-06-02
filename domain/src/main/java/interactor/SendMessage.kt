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

import io.reactivex.Flowable
import model.Attachment
import repository.MessageRepository
import javax.inject.Inject

class SendMessage @Inject constructor(
        private val messageRepo: MessageRepository
) : Interactor<SendMessage.Params>() {

    data class Params(val subId: Int, val threadId: Long, val addresses: List<String>, val body: String, val attachments: List<Attachment> = listOf())

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
                .filter { params.addresses.isNotEmpty() }
                .doOnNext {
                    if (params.addresses.size == 1 && params.attachments.isEmpty()) {
                        messageRepo.sendSmsAndPersist(params.subId, params.threadId, params.addresses.first(), params.body)
                    } else {
                        messageRepo.sendMms(params.subId, params.threadId, params.addresses, params.body, params.attachments)
                    }
                }
                .doOnNext { messageRepo.updateConversations(params.threadId) }
                .doOnNext { messageRepo.markUnarchived(params.threadId) }
    }

}