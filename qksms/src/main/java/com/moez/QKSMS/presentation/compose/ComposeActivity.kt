package com.moez.QKSMS.presentation.compose

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.presentation.base.QkActivity
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.toolbar_chips.*

class ComposeActivity : QkActivity<ComposeViewModel>(), ComposeView {

    override val viewModelClass = ComposeViewModel::class

    override val queryChangedIntent: Subject<CharSequence> = PublishSubject.create()
    override val chipSelectedIntent: Subject<Contact> = PublishSubject.create()
    override val chipDeletedIntent: Subject<Contact> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        chips.layoutManager = FlexboxLayoutManager(this)
        contacts.layoutManager = LinearLayoutManager(this)

        window.callback = ComposeWindowCallback(window.callback, this)
    }

    override fun render(state: ComposeState) {
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
    }

}