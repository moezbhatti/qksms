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
package com.moez.QKSMS.interactor

import com.moez.QKSMS.repository.ConversationRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkBlocked @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val markRead: MarkRead
) : Interactor<MarkBlocked.Params>() {

    data class Params(val threadIds: List<Long>, val blockingClient: Int, val blockReason: String?)

    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params)
                .doOnNext { (threadIds, blockingClient, blockReason) ->
                    conversationRepo.markBlocked(threadIds, blockingClient, blockReason)
                }
                .flatMap { (threadIds) -> markRead.buildObservable(threadIds) }
    }

}
