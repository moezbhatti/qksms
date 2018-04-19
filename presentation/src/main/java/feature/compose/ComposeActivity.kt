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
import common.MenuItemAdapter
import common.QkDialog
import common.base.QkThemedActivity
import common.util.extensions.autoScrollToStart
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import common.util.extensions.showKeyboard
import injection.appComponent
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import model.Contact
import model.Message
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
    override val messageClickIntent: Subject<Message> by lazy { messageAdapter.clicks }
    override val messageLongClickIntent: Subject<Message> by lazy { messageAdapter.longClicks }
    override val menuItemIntent: Subject<Int> by lazy { dialog.adapter.menuItemClicks }
    override val attachmentDeletedIntent: Subject<Uri> by lazy { attachmentAdapter.attachmentDeleted }
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { attach.clicks() }
    override val sendIntent by lazy { send.clicks() }

    @Inject lateinit var chipsAdapter: ChipsAdapter
    @Inject lateinit var contactsAdapter: ContactAdapter
    @Inject lateinit var dialog: QkDialog
    @Inject lateinit var messageAdapter: MessagesAdapter
    @Inject lateinit var attachmentAdapter: AttachmentAdapter

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
                .subscribe { color -> attach.setTint(color) }

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

        chipsAdapter.data = state.selectedContacts
        contactsAdapter.data = state.contacts
        messageAdapter.data = state.messages

        attachments.setVisible(state.attachments.isNotEmpty())
        attachmentAdapter.data = state.attachments

        if (title != state.title) title = state.title

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        send.isEnabled = state.canSend
    }

    override fun setDraft(draft: String) {
        message.setText(draft)
    }

    override fun showMenu(menuItems: List<common.MenuItem>) {
        dialog.adapter.data = menuItems
        dialog.show(this)
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