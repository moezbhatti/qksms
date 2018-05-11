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
package feature.compose

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import common.util.extensions.autoScrollToStart
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import common.util.extensions.showKeyboard
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import model.Contact
import model.Message
import javax.inject.Inject

class ComposeActivity : QkThemedActivity(), ComposeView {

    @Inject lateinit var attachmentAdapter: AttachmentAdapter
    @Inject lateinit var chipsAdapter: ChipsAdapter
    @Inject lateinit var contactsAdapter: ContactAdapter
    @Inject lateinit var messageAdapter: MessagesAdapter
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent: Observable<CharSequence> by lazy { chipsAdapter.textChanges }
    override val queryBackspaceIntent: Observable<*> by lazy { chipsAdapter.backspaces }
    override val queryEditorActionIntent: Observable<Int> by lazy { chipsAdapter.actions }
    override val chipSelectedIntent: Subject<Contact> by lazy { contactsAdapter.contactSelected }
    override val chipDeletedIntent: Subject<Contact> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val messageClickIntent: Subject<Message> by lazy { messageAdapter.clicks }
    override val messagesSelectedIntent by lazy { messageAdapter.selectionChanges }
    override val cancelSendingIntent: Subject<Message> by lazy { messageAdapter.cancelSending }
    override val attachmentDeletedIntent: Subject<Attachment> by lazy { attachmentAdapter.attachmentDeleted }
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { attach.clicks() }
    override val cameraIntent by lazy { camera.clicks() }
    override val galleryIntent by lazy { gallery.clicks() }
    override val inputContentIntent by lazy { message.inputContentSelected }
    override val sendIntent by lazy { send.clicks() }
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ComposeViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        showBackButton(true)
        viewModel.bindView(this)

        chipsAdapter.view = chips

        contacts.itemAnimator = null
        chips.itemAnimator = null
        chips.layoutManager = FlexboxLayoutManager(this)

        val layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        messageAdapter.autoScrollToStart(messageList)
        messageAdapter.emptyView = messagesEmpty

        messageList.setHasFixedSize(true)
        messageList.layoutManager = layoutManager
        messageList.adapter = messageAdapter

        attachments.adapter = attachmentAdapter

        messageBackground.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        message.supportsInputContent = true

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
                .subscribe { tintList -> send.imageTintList = tintList }

        theme
                .autoDisposable(scope())
                .subscribe { color -> send.setBackgroundTint(color) }

        colors.textSecondary
                .autoDisposable(scope())
                .subscribe { color ->
                    attach.setTint(color)
                    camera.setTint(color)
                    gallery.setTint(color)
                }

        colors.bubble
                .autoDisposable(scope())
                .subscribe { color -> messageBackground.setBackgroundTint(color) }

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> contacts.setBackgroundColor(color) }

        colors.composeBackground
                .doOnNext { color -> composeBackground.setBackgroundTint(color) }
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

        title = when (state.selectedMessages) {
            0 -> state.conversationtitle
            else -> getString(R.string.compose_title_selected, state.selectedMessages)
        }

        toolbarTitle.setVisible(!state.editingMode)
        chips.setVisible(state.editingMode)
        contacts.setVisible(state.contactsVisible)
        composeBar.setVisible(!state.contactsVisible)

        // Don't set the adapters unless needed
        if (state.editingMode && chips.adapter == null) chips.adapter = chipsAdapter
        if (state.editingMode && contacts.adapter == null) contacts.adapter = contactsAdapter

        toolbar.menu.findItem(R.id.call)?.isVisible = !state.editingMode && state.selectedMessages == 0
        toolbar.menu.findItem(R.id.info)?.isVisible = !state.editingMode && state.selectedMessages == 0
        toolbar.menu.findItem(R.id.copy)?.isVisible = !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.delete)?.isVisible = !state.editingMode && state.selectedMessages > 0
        toolbar.menu.findItem(R.id.forward)?.isVisible = !state.editingMode && state.selectedMessages == 1

        if (chipsAdapter.data.isEmpty() && state.selectedContacts.isNotEmpty()) {
            message.showKeyboard()
        }

        chipsAdapter.data = state.selectedContacts
        contactsAdapter.data = state.contacts
        messageAdapter.data = state.messages

        attachments.setVisible(state.attachments.isNotEmpty())
        attachmentAdapter.data = state.attachments

        attach.animate().rotation(if (state.attaching) 45f else 0f).start()
        camera.setVisible(state.attaching)
        gallery.setVisible(state.attaching)

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        send.isEnabled = state.canSend
    }

    override fun clearSelection() {
        messageAdapter.clearSelection()
    }

    override fun setDraft(draft: String) {
        message.setText(draft)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        backPressedIntent.onNext(Unit)
    }

}