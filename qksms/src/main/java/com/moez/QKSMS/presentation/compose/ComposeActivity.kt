package com.moez.QKSMS.presentation.compose

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.presentation.base.QkActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.toolbar_chips.*
import javax.inject.Inject


class ComposeActivity : QkActivity<ComposeViewModel>(), ComposeView {

    @Inject lateinit var themeManager: ThemeManager

    private lateinit var layoutManager: LinearLayoutManager

    override val viewModelClass = ComposeViewModel::class
    override val queryChangedIntent: Observable<CharSequence> by lazy { chipsAdapter.textChanges }
    override val chipSelectedIntent: Subject<Contact> by lazy { contactsAdapter.contactSelected }
    override val chipDeletedIntent: Subject<Contact> by lazy { chipsAdapter.chipDeleted }
    override val copyTextIntent: Subject<Message> = PublishSubject.create()
    override val forwardMessageIntent: Subject<Message> = PublishSubject.create()
    override val deleteMessageIntent: Subject<Message> = PublishSubject.create()
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { attach.clicks() }
    override val sendIntent by lazy { send.clicks() }

    private val chipsAdapter by lazy { ChipsAdapter(this, chips) }
    private val contactsAdapter by lazy { ContactAdapter(this) }
    private val messageAdapter by lazy { MessagesAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppComponentManager.appComponent.inject(this)
        setContentView(R.layout.compose_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        chips.layoutManager = FlexboxLayoutManager(this)
        chips.adapter = chipsAdapter

        contacts.layoutManager = LinearLayoutManager(this)
        contacts.adapter = contactsAdapter

        setupMessagesAdapter()

        layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        messageList.layoutManager = layoutManager
        messageList.adapter = messageAdapter

        window.callback = ComposeWindowCallback(window.callback, this)
    }

    override fun render(state: ComposeState) {
        toolbarTitle.setVisible(!state.editingMode)
        chips.setVisible(state.editingMode)
        contacts.setVisible(state.editingMode)
        composeBar.setVisible(!state.editingMode || state.selectedContacts.isNotEmpty())

        if (chipsAdapter.data !== state.selectedContacts) {
            chipsAdapter.data = state.selectedContacts
        }

        if (contactsAdapter.data !== state.contacts) {
            contactsAdapter.data = state.contacts
        }

        if (messageAdapter.data !== state.messages) {
            messageAdapter.updateData(state.messages)
        }

        if (title != state.title) title = state.title
        if (message.text.toString() != state.draft) message.setText(state.draft)

        send.setTint(if (state.canSend) themeManager.color else resources.getColor(R.color.textTertiary))
        send.isEnabled = state.canSend
    }

    private fun setupMessagesAdapter() {
        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

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
    }
}