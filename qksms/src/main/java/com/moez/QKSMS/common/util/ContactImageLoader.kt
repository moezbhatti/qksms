package com.moez.QKSMS.common.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.security.MessageDigest


class ContactImageLoader(val context: Context) : ModelLoader<Uri, InputStream> {

    override fun handles(model: Uri?): Boolean {
        return model.toString().startsWith("content://com.android.contacts/contacts/")
    }

    override fun buildLoadData(model: Uri, width: Int, height: Int, options: Options?): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ContactImageKey(model), ContactImageFetcher(context, model))
    }

    class Factory(val context: Context) : ModelLoaderFactory<Uri, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory) = ContactImageLoader(context)
        override fun teardown() {} // nothing to do here
    }

    class ContactImageKey(private val contactUri: Uri) : Key {
        override fun updateDiskCacheKey(digest: MessageDigest) = digest.update(contactUri.toString().toByteArray())
    }

    class ContactImageFetcher(private val context: Context, private val contactUri: Uri) : DataFetcher<InputStream> {

        private var loadPhotoDisposable: Disposable? = null

        override fun cleanup() {}
        override fun getDataClass() = InputStream::class.java
        override fun getDataSource() = DataSource.LOCAL

        override fun loadData(priority: Priority?, callback: DataFetcher.DataCallback<in InputStream>) {
            loadPhotoDisposable = Maybe.just(contactUri)
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