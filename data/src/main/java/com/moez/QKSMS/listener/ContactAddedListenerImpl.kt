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
package com.moez.QKSMS.listener

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import com.moez.QKSMS.repository.SyncRepository
import io.reactivex.Observable
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

    override fun listen(address: String): Observable<*> {
        return ContactContentObserver(context).observable
                .filter { syncRepo.syncContact(address) }
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