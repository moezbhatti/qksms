package com.moez.QKSMS.common.util

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
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.data.repository.ContactRepository
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject


class ContactImageLoader(val context: Context) : ModelLoader<String, InputStream> {

    override fun handles(model: String?): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(model)
    }

    override fun buildLoadData(model: String, width: Int, height: Int, options: Options?): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ContactImageKey(model), ContactImageFetcher(context, model))
    }

    class Factory(val context: Context) : ModelLoaderFactory<String, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory) = ContactImageLoader(context)
        override fun teardown() {} // nothing to do here
    }

    class ContactImageKey(private val address: String) : Key {
        override fun updateDiskCacheKey(digest: MessageDigest) = digest.update(address.toByteArray())
    }

    class ContactImageFetcher(private val context: Context, private val address: String) : DataFetcher<InputStream> {

        @Inject lateinit var contactRepo: ContactRepository
        private var loadPhotoDisposable: Disposable? = null

        init {
            appComponent.inject(this)
        }

        override fun cleanup() {}
        override fun getDataClass() = InputStream::class.java
        override fun getDataSource() = DataSource.LOCAL

        override fun loadData(priority: Priority?, callback: DataFetcher.DataCallback<in InputStream>) {
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