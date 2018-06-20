package com.moez.QKSMS.feature.compose

import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.net.Uri
import com.moez.QKSMS.injection.ViewModelKey
import com.moez.QKSMS.model.Attachment
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import java.net.URLDecoder
import javax.inject.Named

@Module
class ComposeActivityModule {

    @Provides
    @Named("query")
    fun provideQuery(activity: ComposeActivity): String = activity.intent.extras?.getString("query") ?: ""

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: ComposeActivity): Long = activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @Named("address")
    fun provideAddress(activity: ComposeActivity): String {
        var address = ""

        activity.intent.data?.let {
            val data = it.toString()
            address = when {
                it.scheme.startsWith("smsto") -> data.replace("smsto:", "")
                it.scheme.startsWith("mmsto") -> data.replace("mmsto:", "")
                it.scheme.startsWith("sms") -> data.replace("sms:", "")
                it.scheme.startsWith("mms") -> data.replace("mms:", "")
                else -> ""
            }

            // The dialer app on Oreo sends a URL encoded string, make sure to decode it
            if (address.contains('%')) address = URLDecoder.decode(address, "UTF-8")
        }

        return address
    }

    @Provides
    @Named("text")
    fun provideSharedText(activity: ComposeActivity): String {
        return activity.intent.extras?.getString(Intent.EXTRA_TEXT) ?: ""
    }

    @Provides
    @Named("attachments")
    fun provideSharedAttachments(activity: ComposeActivity): List<Attachment> {
        val sharedImages = mutableListOf<Uri>()
        activity.intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.run(sharedImages::add)
        activity.intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.run(sharedImages::addAll)
        return sharedImages.map { Attachment(it) }
    }

    @Provides
    @IntoMap
    @ViewModelKey(ComposeViewModel::class)
    fun provideComposeViewModel(viewModel: ComposeViewModel): ViewModel = viewModel

}