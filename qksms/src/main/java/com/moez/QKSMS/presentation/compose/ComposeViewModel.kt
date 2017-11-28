package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.presentation.base.QkViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject


class ComposeViewModel : QkViewModel<ComposeView, ComposeState>(ComposeState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var contactsRepo: ContactRepository

    private val contactsSubject: Subject<List<Contact>> = BehaviorSubject.create()
    private val selectedContactsReducer: Subject<(List<Contact>) -> List<Contact>> = BehaviorSubject.create()

    private val contacts: List<Contact>

    init {
        AppComponentManager.appComponent.inject(this)

        contacts = contactsRepo.getContacts()

        val contactsFlowable = contactsSubject.toFlowable(BackpressureStrategy.BUFFER)

        val selectedContacts: Flowable<List<Contact>> = selectedContactsReducer
                .scan(listOf<Contact>(), { previousState, reducer -> reducer(previousState) })
                .toFlowable(BackpressureStrategy.BUFFER)

        newState { it.copy(contacts = contactsFlowable, selectedContacts = selectedContacts) }
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        intents += view.queryChangedIntent
                .toFlowable(BackpressureStrategy.LATEST)
                .map { query -> query.toString() }
                .map { query ->
                    contacts.filter { contact ->
                        contact.name.contains(query, true) || PhoneNumberUtils.compare(contact.address, query)
                    }
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contacts -> contactsSubject.onNext(contacts) }

        intents += view.chipSelectedIntent.subscribe { contact ->
            selectedContactsReducer.onNext { it.toMutableList().apply { add(contact) } }
        }

        intents += view.chipDeletedIntent.subscribe { contact ->
            selectedContactsReducer.onNext { it.filterNot { it == contact } }
        }
    }

}