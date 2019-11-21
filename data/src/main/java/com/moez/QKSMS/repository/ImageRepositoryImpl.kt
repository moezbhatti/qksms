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
package com.moez.QKSMS.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(private val context: Context) : ImageRepository {

    override fun loadImage(uri: Uri, width: Int, height: Int): Bitmap? {
        val orientation = context.contentResolver.openInputStream(uri)?.use(::ExifInterface)
                ?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotated = orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270

        // Determine the dimensions
        val dimensionsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, dimensionsOptions)
        val srcWidth = if (rotated) dimensionsOptions.outHeight else dimensionsOptions.outWidth
        val srcHeight = if (rotated) dimensionsOptions.outWidth else dimensionsOptions.outHeight

        // If we get the dimensions and they don't exceed the max size, we don't need to scale
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = if ((width == 0 || srcWidth < width) && (height == 0 || srcHeight < height)) {
            BitmapFactory.decodeStream(inputStream)
        } else {
            val widthScaleFactor = width.toDouble() / srcWidth
            val heightScaleFactor = height.toDouble() / srcHeight
            val options = when {
                widthScaleFactor > heightScaleFactor -> BitmapFactory.Options().apply {
                    inScaled = true
                    inSampleSize = 4
                    inDensity = srcHeight
                    inTargetDensity = height * inSampleSize
                }

                else -> BitmapFactory.Options().apply {
                    inScaled = true
                    inSampleSize = 4
                    inDensity = srcWidth
                    inTargetDensity = width * inSampleSize
                }
            }
            BitmapFactory.decodeStream(inputStream, null, options) ?: return null
        }

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height

        val mtx = Matrix()
        mtx.postRotate(degree)

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true)
    }

}
