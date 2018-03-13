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
package presentation.feature.compose

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import common.util.extensions.showKeyboard
import data.model.Contact
import data.model.Message
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.toolbar_chips.*
import presentation.common.base.QkThemedActivity
import javax.inject.Inject


class ComposeActivity : QkThemedActivity<ComposeViewModel>(), ComposeView {

    override val viewModelClass = ComposeViewModel::class
    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent: Observable<CharSequence> by lazy { chipsAdapter.textChanges }
    override val queryKeyEventIntent: Observable<KeyEvent> by lazy { chipsAdapter.keyEvents }
    override val queryEditorActionIntent: Observable<Int> by lazy { chipsAdapter.actions }
    override val chipSelectedIntent: Subject<Contact> by lazy { contactsAdapter.contactSelected }
    override val chipDeletedIntent: Subject<Contact> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val callIntent: Subject<Unit> = PublishSubject.create()
    override val infoIntent: Subject<Unit> = PublishSubject.create()
    override val copyTextIntent: Subject<Message> = PublishSubject.create()
    override val forwardMessageIntent: Subject<Message> = PublishSubject.create()
    override val deleteMessageIntent: Subject<Message> = PublishSubject.create()
    override val messageClickIntent: Subject<Message> by lazy { messageAdapter.clicks }
    override val attachmentDeletedIntent: Subject<Uri> by lazy { attachmentAdapter.attachmentDeleted }
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { attach.clicks() }
    override val sendIntent by lazy { send.clicks() }

    @Inject lateinit var chipsAdapter: ChipsAdapter
    @Inject lateinit var contactsAdapter: ContactAdapter
    @Inject lateinit var messageAdapter: MessagesAdapter
    @Inject lateinit var attachmentAdapter: AttachmentAdapter

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        chipsAdapter.view = chips

        chips.itemAnimator = null
        chips.layoutManager = FlexboxLayoutManager(this)

        contacts.itemAnimator = null
        contacts.layoutManager = LinearLayoutManager(this)

        val layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart > 0) {
                    messageAdapter.notifyItemChanged(positionStart - 1)
                }

                // If we're at the bottom, scroll down to show new messages
                val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (positionStart >= messageAdapter.itemCount - 1 && lastVisiblePosition == positionStart - 1) {
                    messageList.scrollToPosition(positionStart)
                }
            }
        })

        messageAdapter.longClicks.subscribe { message ->
            AlertDialog.Builder(this)
                    .setItems(R.array.message_options, { _, row ->
                        when (row) {
                            0 -> copyTextIntent.onNext(message)
                            1 -> forwardMessageIntent.onNext(message)
                            2 -> deleteMessageIntent.onNext(message)
                        }
                    })
                    .show()
        }

        messageList.setHasFixedSize(true)
        messageList.layoutManager = layoutManager
        messageList.adapter = messageAdapter

        attachments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachments.adapter = attachmentAdapter

        messageBackground.backgroundTintMode = PorterDuff.Mode.MULTIPLY

        val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled))

        val iconEnabled = threadId
                .distinctUntilChanged()
                .switchMap { threadId -> colors.textPrimaryOnThemeForConversation(threadId) }

        val iconDisabled = threadId
                .distinctUntilChanged()
                .switchMap { threadId -> colors.textTertiaryOnThemeForConversation(threadId) }

        Observables
                .combineLatest(iconEnabled, iconDisabled, { primary, tertiary ->
                    ColorStateList(states, intArrayOf(primary, tertiary))
                })
                .autoDisposable(scope())
                .subscribe { tintList -> sendIcon.imageTintList = tintList }

        theme
                .autoDisposable(scope())
                .subscribe { color -> send.setBackgroundTint(color) }

        colors.textSecondary
                .autoDisposable(scope())
                .subscribe { color -> attach.setTint(color) }

        colors.bubble
                .autoDisposable(scope())
                .subscribe { color -> messageBackground.setBackgroundTint(color) }

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> contacts.setBackgroundColor(color) }

        colors.composeBackground
                .doOnNext { color -> composeBar.setBackgroundTint(color) }
                .doOnNext { color -> window.decorView.setBackgroundColor(color) }
                .autoDisposable(scope())
                .subscribe()

        window.callback = ComposeWindowCallback(window.callback, this)
    }

    override fun onStart() {
        super.onStart()
        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    override fun render(state: ComposeState) {
        if (state.hasError) {
            finish()
            return
        }

        threadId.onNext(state.selectedConversation)

        toolbarTitle.setVisible(!state.editingMode)
        chips.setVisible(state.editingMode)
        contacts.setVisible(state.contactsVisible)
        composeBar.setVisible(!state.contactsVisible)

        // Don't set the adapters unless needed
        if (state.editingMode && chips.adapter == null) chips.adapter = chipsAdapter
        if (state.editingMode && contacts.adapter == null) contacts.adapter = contactsAdapter

        toolbar.menu.findItem(R.id.call)?.isVisible = !state.editingMode
        toolbar.menu.findItem(R.id.info)?.isVisible = !state.editingMode

        if (chipsAdapter.data.isEmpty() && state.selectedContacts.isNotEmpty()) {
            message.showKeyboard()
        }

        if (chipsAdapter.data !== state.selectedContacts) {
            chipsAdapter.data = state.selectedContacts
        }

        if (contactsAdapter.data !== state.contacts) {
            contactsAdapter.data = state.contacts
        }

        if (messageAdapter.data !== state.messages) {
            messageAdapter.data = state.messages
        }

        attachments.setVisible(state.attachments.isNotEmpty())
        if (attachmentAdapter.data !== state.attachments) {
            attachmentAdapter.data = state.attachments
        }

        if (title != state.title) title = state.title

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        send.isEnabled = state.canSend
        sendIcon.isEnabled = state.canSend
    }

    override fun setDraft(draft: String) {
        message.setText(draft)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.call -> callIntent.onNext(Unit)
            R.id.info -> infoIntent.onNext(Unit)
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

}