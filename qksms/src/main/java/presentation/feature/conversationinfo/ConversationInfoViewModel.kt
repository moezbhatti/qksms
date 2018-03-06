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
package presentation.feature.conversationinfo

import android.content.Context
import android.content.Intent
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.extensions.asObservable
import data.model.Conversation
import data.repository.MessageRepository
import interactor.*
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import presentation.common.Navigator
import presentation.common.base.QkViewModel
import javax.inject.Inject

class ConversationInfoViewModel(intent: Intent) : QkViewModel<ConversationInfoView,
        ConversationInfoState>(ConversationInfoState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var markArchived: MarkArchived
    @Inject lateinit var markUnarchived: MarkUnarchived
    @Inject lateinit var markBlocked: MarkBlocked
    @Inject lateinit var markUnblocked: MarkUnblocked
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var deleteConversation: DeleteConversation

    private val conversation: Observable<Conversation>

    init {
        appComponent.inject(this)
        val threadId = intent.extras?.getLong("threadId") ?: 0L

        conversation = messageRepo.getConversationAsync(threadId)
                .asObservable<Conversation>()
                .filter { conversation -> conversation.isLoaded }
                .doOnNext { conversation ->
                    if (!conversation.isValid) {
                        newState { it.copy(hasError = true) }
                    }
                }
                .filter { conversation -> conversation.isValid }
                .filter { conversation -> conversation.id != 0L }

        disposables += markArchived
        disposables += markUnarchived
        disposables += markBlocked
        disposables += markUnblocked
        disposables += deleteConversation

        // Update the recipients whenever they change
        disposables += conversation
                .map { conversation -> conversation.recipients }
                .distinctUntilChanged()
                .subscribe { recipients -> newState { it.copy(recipients = recipients) } }

        // Update the view's archived state whenever it changes
        disposables += conversation
                .map { conversation -> conversation.archived }
                .distinctUntilChanged()
                .subscribe { archived -> newState { it.copy(archived = archived) } }

        // Update the view's blocked state whenever it changes
        disposables += conversation
                .map { conversation -> conversation.blocked }
                .distinctUntilChanged()
                .subscribe { blocked -> newState { it.copy(blocked = blocked) } }

        // Load the attachments from the conversation
        disposables += messageRepo.getPartsForConversation(threadId)
                .map { parts -> parts.filter { part -> part.isImage() } }
                .map { parts -> parts.reversed() }
                .subscribe { media -> newState { it.copy(media = media) } }
    }

    override fun bindView(view: ConversationInfoView) {
        super.bindView(view)

        // Show the notifications settings for the conversation
        view.notificationsIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation -> navigator.showNotificationSettings(conversation.id) }

        // Toggle the archived state of the conversation
        view.archiveIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation ->
                    when (conversation.archived) {
                        true -> markUnarchived.execute(conversation.id)
                        false -> markArchived.execute(conversation.id)
                    }
                }

        // Toggle the blocked state of the conversation
        view.blockIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation ->
                    when (conversation.blocked) {
                        true -> markUnblocked.execute(conversation.id)
                        false -> markBlocked.execute(conversation.id)
                    }
                }

        // Show the delete confirmation dialog
        view.deleteIntent
                .autoDisposable(view.scope())
                .subscribe { view.showDeleteDialog() }

        // Delete the conversation
        view.confirmDeleteIntent
                .withLatestFrom(conversation, { _, conversation -> conversation })
                .autoDisposable(view.scope())
                .subscribe { conversation -> deleteConversation.execute(conversation.id) }
    }

}