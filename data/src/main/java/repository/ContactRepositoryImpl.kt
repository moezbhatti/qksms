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
package repository

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import model.Contact
import util.extensions.asFlowable
import util.extensions.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(val context: Context) : ContactRepository {

    override fun findContactUri(address: String): Single<Uri> {
        return Flowable.just(address)
                .map {
                    when {
                        address.contains('@') -> Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(address))
                        else -> Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
                    }
                }
                .mapNotNull { uri -> context.contentResolver.query(uri, arrayOf(BaseColumns._ID), null, null, null) }
                .flatMap { cursor -> cursor.asFlowable() }
                .firstOrError()
                .map { cursor -> cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID)) }
                .map { id -> Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id) }
    }

    override fun getUnmanagedContacts(): Flowable<List<Contact>> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Contact::class.java)
                .sort("name")
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .filter { it.isValid }
                .map { realm.copyFromRealm(it) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
    }

}