package com.moez.QKSMS.presentation.compose

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.presentation.base.QkActivity
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.toolbar_chips.*
import javax.inject.Inject


class ComposeActivity : QkActivity<ComposeViewModel>(), ComposeView {

    @Inject lateinit var themeManager: ThemeManager

    private lateinit var layoutManager: LinearLayoutManager

    override val viewModelClass = ComposeViewModel::class
    override val queryChangedIntent: Subject<CharSequence> = PublishSubject.create()
    override val chipSelectedIntent: Subject<Contact> = PublishSubject.create()
    override val chipDeletedIntent: Subject<Contact> = PublishSubject.create()
    override val copyTextIntent: Subject<Message> = PublishSubject.create()
    override val forwardMessageIntent: Subject<Message> = PublishSubject.create()
    override val deleteMessageIntent: Subject<Message> = PublishSubject.create()
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy { attach.clicks() }
    override val sendIntent by lazy { send.clicks() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppComponentManager.appComponent.inject(this)
        setContentView(R.layout.compose_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        chips.layoutManager = FlexboxLayoutManager(this)
        contacts.layoutManager = LinearLayoutManager(this)

        window.callback = ComposeWindowCallback(window.callback, this)

        layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messageList.layoutManager = layoutManager
    }

    override fun render(state: ComposeState) {
        toolbarTitle.visibility = if (state.editingMode) View.GONE else View.VISIBLE
        chips.visibility = if (state.editingMode) View.VISIBLE else View.GONE
        contacts.visibility = if (state.editingMode) View.VISIBLE else View.GONE

        if (chips.adapter == null && state.selectedContacts != null) {
            val adapter = ChipsAdapter(this, chips, state.selectedContacts)
            adapter.chipDeleted.subscribe { chipDeletedIntent.onNext(it) }
            adapter.textChanges.subscribe { queryChangedIntent.onNext(it) }

            chips.adapter = adapter
        }

        if (contacts.adapter == null && state.contacts != null) {
            val adapter = ContactAdapter(this, state.contacts)
            adapter.contactSelected.subscribe { chipSelectedIntent.onNext(it) }

            contacts.adapter = adapter
        }

        if (title != state.title) title = state.title
        if (messageList.adapter == null && state.messages?.isValid == true) messageList.adapter = createAdapter(state.messages)
        if (message.text.toString() != state.draft) message.setText(state.draft)

        send.setTint(if (state.canSend) themeManager.color else resources.getColor(R.color.textTertiary))
        send.isEnabled = state.canSend
    }

    private fun createAdapter(messages: RealmResults<Message>): MessagesAdapter {
        val adapter = MessagesAdapter(messages)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                viewModel.dataChanged()

                if (positionStart > 0) {
                    adapter.notifyItemChanged(positionStart - 1)
                }

                // If we're at the bottom, scroll down to show new messages
                val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (positionStart >= adapter.itemCount - 1 && lastVisiblePosition == positionStart - 1) {
                    messageList.scrollToPosition(positionStart)
                }
            }
        })

        adapter.longClicks.subscribe { message ->
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

        return adapter
    }
}