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
package feature.blocked

import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import injection.appComponent
import interactor.MarkUnblocked
import io.reactivex.rxkotlin.withLatestFrom
import common.base.QkViewModel
import repository.MessageRepository
import javax.inject.Inject

class BlockedViewModel : QkViewModel<BlockedView, BlockedState>(BlockedState()) {

    @Inject lateinit var markUnblocked: MarkUnblocked
    @Inject lateinit var messageRepo: MessageRepository

    init {
        appComponent.inject(this)

        val conversations = messageRepo.getBlockedConversations()
                .doOnNext { conversations -> newState { it.copy(empty = conversations.isEmpty()) } }

        newState { it.copy(data = conversations) }
    }

    override fun bindView(view: BlockedView) {
        super.bindView(view)

        // Show confirm unblock conversation dialog
        view.unblockIntent
                .autoDisposable(view.scope())
                .subscribe { view.showUnblockDialog() }

        // Unblock conversation
        view.confirmUnblockIntent
                .withLatestFrom(view.unblockIntent, { _, threadId -> threadId })
                .autoDisposable(view.scope())
                .subscribe { threadId -> markUnblocked.execute(threadId) }
    }

}