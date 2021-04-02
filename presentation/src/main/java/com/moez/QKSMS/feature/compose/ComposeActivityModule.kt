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
package com.moez.QKSMS.feature.compose

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.android.mms.ContentType
import com.moez.QKSMS.injection.ViewModelKey
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.model.Attachments
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import java.net.URLDecoder
import java.nio.charset.Charset
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
    @Named("addresses")
    fun provideAddresses(activity: ComposeActivity): List<String> {
        return activity.intent
                ?.decodedDataString()
                ?.substringAfter(':') // Remove scheme
                ?.substringBefore("?") // Remove query
                ?.split(",", ";")
                ?.filter { number -> number.isNotEmpty() }
                ?: listOf()
    }

    @Provides
    @Named("text")
    fun provideSharedText(activity: ComposeActivity): String {
        var subject = activity.intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: "";
        if (subject != "") {
            subject += "\n"
        }

        return subject + (activity.intent.extras?.getString(Intent.EXTRA_TEXT)
                ?: activity.intent.extras?.getString("sms_body")
                ?: activity.intent?.decodedDataString()
                        ?.substringAfter('?') // Query string
                        ?.takeIf { it.startsWith("body") }
                        ?.substringAfter('=')
                ?: "")
    }

    @Provides
    @Named("attachments")
    fun provideSharedAttachments(activity: ComposeActivity): Attachments {
        val uris = mutableListOf<Uri>()
        activity.intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.run(uris::add)
        activity.intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.run(uris::addAll)
        return Attachments(uris.mapNotNull { uri ->
            val mimeType = activity.contentResolver.getType(uri)
            when {
                ContentType.isImageType(mimeType) -> {
                    Attachment.Image(uri)
                }

                ContentType.TEXT_VCARD.equals(mimeType, true) -> {
                    val inputStream = activity.contentResolver.openInputStream(uri)
                    val text = inputStream?.reader(Charset.forName("utf-8"))?.readText()
                    text?.let(Attachment::Contact)
                }

                else -> null
            }
        })
    }

    @Provides
    @IntoMap
    @ViewModelKey(ComposeViewModel::class)
    fun provideComposeViewModel(viewModel: ComposeViewModel): ViewModel = viewModel

    // The dialer app on Oreo sends a URL encoded string, make sure to decode it
    private fun Intent.decodedDataString(): String? {
        val data = data?.toString()
        if (data?.contains('%') == true) {
            return URLDecoder.decode(data, "UTF-8")
        }
        return data
    }

}
