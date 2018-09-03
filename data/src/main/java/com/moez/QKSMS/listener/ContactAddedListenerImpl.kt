package com.moez.QKSMS.listener

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import com.moez.QKSMS.repository.SyncRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Listens for a contact being added, and then syncs it to Realm
 *
 * TODO: Stop listening automatically. Currently, this will only happen if the contact is added
 */
class ContactAddedListenerImpl @Inject constructor(
        private val context: Context,
        private val syncRepo: SyncRepository
) : ContactAddedListener {

    companion object {
        private val URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    }

    override fun listen(address: String): Single<*> {
        val observable = ContactContentObserver(context).observable
                .filter { syncRepo.syncContact(address) }

        return Single.fromObservable(observable)
    }

    private class ContactContentObserver(context: Context) : ContentObserver(Handler()) {

        private val subject = PublishSubject.create<Unit>()

        val observable: Observable<Unit> = subject
                .doOnSubscribe { context.contentResolver.registerContentObserver(URI, true, this) }
                .doOnDispose { context.contentResolver.unregisterContentObserver(this) }
                .share()

        override fun onChange(selfChange: Boolean) {
            this.onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            subject.onNext(Unit)
        }

    }

}