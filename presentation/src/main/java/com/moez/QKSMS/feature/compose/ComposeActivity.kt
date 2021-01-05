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
import android.os.Bundle
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.google.android.mms.ContentType
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.databinding.ContainerActivityBinding
import com.moez.QKSMS.model.Attachment
import dagger.android.AndroidInjection
import java.net.URLDecoder
import java.nio.charset.Charset

class ComposeActivity : QkThemedActivity() {

    private val binding by viewBinding(ContainerActivityBinding::inflate)
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        toolbar.isVisible = false

        router = Conductor.attachRouter(this, binding.container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(ComposeController(
                    getQuery(), getThreadId(), getAddresses(), getSharedText(), getSharedAttachments())))
        }
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    private fun getQuery(): String = intent.extras?.getString("query") ?: ""

    private fun getThreadId(): Long = intent.extras?.getLong("threadId") ?: 0L

    private fun getAddresses(): List<String> {
        return intent
                ?.decodedDataString()
                ?.substringAfter(':') // Remove scheme
                ?.replaceAfter("?", "") // Remove query
                ?.split(",", ";")
                ?: listOf()
    }

    private fun getSharedText(): String {
        var subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: "";
        if (subject != "") {
            subject += "\n"
        }

        return subject + (intent.extras?.getString(Intent.EXTRA_TEXT)
                ?: intent.extras?.getString("sms_body")
                ?: intent?.decodedDataString()
                        ?.substringAfter('?') // Query string
                        ?.split(',')
                        ?.firstOrNull { param -> param.startsWith("body") }
                        ?.substringAfter('=')
                ?: "")
    }

    private fun getSharedAttachments(): List<Attachment> {
        val uris = mutableListOf<Uri>()
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.run(uris::add)
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.run(uris::addAll)
        return uris.mapNotNull { uri ->
            val mimeType = contentResolver.getType(uri)
            when {
                ContentType.isImageType(mimeType) -> {
                    Attachment.Image(uri)
                }

                ContentType.TEXT_VCARD.equals(mimeType, true) -> {
                    val inputStream = contentResolver.openInputStream(uri)
                    val text = inputStream?.reader(Charset.forName("utf-8"))?.readText()
                    text?.let(Attachment::Contact)
                }

                else -> null
            }
        }
    }

    // The dialer app on Oreo sends a URL encoded string, make sure to decode it
    private fun Intent.decodedDataString(): String? {
        val data = data?.toString()
        if (data?.contains('%') == true) {
            return URLDecoder.decode(data, "UTF-8")
        }
        return data
    }

}
