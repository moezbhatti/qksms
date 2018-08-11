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
package com.moez.QKSMS.util

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.moez.QKSMS.repository.ContactRepository
import com.moez.QKSMS.repository.ContactRepositoryImpl
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.security.MessageDigest


class ContactImageLoader(
        private val context: Context,
        private val contactRepo: ContactRepository
) : ModelLoader<String, InputStream> {

    override fun handles(model: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(model)
    }

    override fun buildLoadData(model: String, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ContactImageKey(model), ContactImageFetcher(context, contactRepo, model))
    }

    class Factory(val context: Context, val prefs: Preferences) : ModelLoaderFactory<String, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory) = ContactImageLoader(context, ContactRepositoryImpl(context, prefs))
        override fun teardown() {} // nothing to do here
    }

    class ContactImageKey(private val address: String) : Key {
        override fun updateDiskCacheKey(digest: MessageDigest) = digest.update(address.toByteArray())
    }

    class ContactImageFetcher(
            private val context: Context,
            private val contactRepo: ContactRepository,
            private val address: String
    ) : DataFetcher<InputStream> {

        private var loadPhotoDisposable: Disposable? = null

        override fun cleanup() {}
        override fun getDataClass() = InputStream::class.java
        override fun getDataSource() = DataSource.LOCAL

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
            loadPhotoDisposable = contactRepo.findContactUri(address)
                    .map { uri -> ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, uri) }
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { inputStream -> callback.onDataReady(inputStream) },
                            { error -> callback.onLoadFailed(Exception(error)) })
        }

        override fun cancel() {
            loadPhotoDisposable?.dispose()
        }
    }

}