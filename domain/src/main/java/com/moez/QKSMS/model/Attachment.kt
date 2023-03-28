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
package com.moez.QKSMS.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.view.inputmethod.InputContentInfoCompat

sealed class Attachment {
    data class Image(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null
    ) : Attachment() {

        fun getUri(): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri
            }
        }

        fun isGif(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && inputContent != null) {
                inputContent.description.hasMimeType("image/gif")
            } else {
                uri?.let(context.contentResolver::getType) == "image/gif"
            }
        }
    }

    data class Video(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null
    ) : Attachment() {
        fun getUri(): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri
            }
        }

        fun getContentType(context: Context): String? {
            return getUri()?.let(context.contentResolver::getType)
        }
    }

    data class File(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null
    ) : Attachment() {

        fun getUri(): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri
            }
        }

        fun getContentType(context: Context): String? {
            return getUri()?.let(context.contentResolver::getType)
        }

        fun getName(context: Context): String? {
            return getUri()?.let {
                // The selection parameter can be null since the intent only opens one file.
                returnUri ->
                context.contentResolver.query(returnUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            }?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                return cursor.getString(nameIndex)
            }
        }

        fun getSize(context: Context): Long? {
            return getUri()?.let {
                // The selection parameter can be null since the intent only opens one file.
                returnUri ->
                context.contentResolver.query(returnUri, arrayOf(OpenableColumns.SIZE), null, null, null)
            }?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                return cursor.getLong(sizeIndex)
            }
        }
    }

    data class Contact(val vCard: String) : Attachment()
}

class Attachments(attachments: List<Attachment>) : List<Attachment> by attachments
