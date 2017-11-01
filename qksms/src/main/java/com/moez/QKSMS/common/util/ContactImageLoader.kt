package com.moez.QKSMS.common.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import android.telephony.PhoneNumberUtils
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.moez.QKSMS.common.util.extensions.asFlowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.security.MessageDigest


class ContactImageLoader(val context: Context) : ModelLoader<String, InputStream> {

    override fun handles(model: String?): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(model)
    }

    override fun buildLoadData(model: String?, width: Int, height: Int, options: Options?): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ContactImageKey(model.orEmpty()), ContactImageFetcher(context, model.orEmpty()))
    }

    class Factory(val context: Context) : ModelLoaderFactory<String, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory) = ContactImageLoader(context)
        override fun teardown() {} // nothing to do here
    }

    class ContactImageKey(val address: String) : Key {
        override fun updateDiskCacheKey(digest: MessageDigest) = digest.update(address.toByteArray())
    }

    class ContactImageFetcher(val context: Context, val address: String) : DataFetcher<InputStream> {

        private var loadPhotoDisposable: Disposable? = null

        override fun cleanup() {}
        override fun getDataClass() = InputStream::class.java
        override fun getDataSource() = DataSource.LOCAL

        override fun loadData(priority: Priority?, callback: DataFetcher.DataCallback<in InputStream>) {
            loadPhotoDisposable = Flowable.just(address)
                    .map { address -> Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)) }
                    .flatMap { uri -> context.contentResolver.query(uri, arrayOf(PhoneLookup._ID), null, null, null).asFlowable() }
                    .firstOrError()
                    .map { cursor -> cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup._ID)) }
                    .map { id -> Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id) }
                    .map { uri -> ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, uri) }
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { inputStream -> callback.onDataReady(inputStream) },
                            { callback.onLoadFailed(Exception(it)) })
        }

        override fun cancel() {
            loadPhotoDisposable?.dispose()
        }
    }

}