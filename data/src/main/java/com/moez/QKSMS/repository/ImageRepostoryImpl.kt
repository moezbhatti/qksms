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
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class ImageRepostoryImpl @Inject constructor(private val context: Context) : ImageRepository {

    override fun loadImage(uri: Uri): Bitmap? {
        val exif = ExifInterface(context.contentResolver.openInputStream(uri))
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)


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

    override fun saveImage(uri: Uri) {
        val type = context.contentResolver.getType(uri)?.split("/") ?: return
        val dir = File(Environment.getExternalStorageDirectory(), "QKSMS").apply { mkdirs() }
        val file = File(dir, "${type.first()}${System.currentTimeMillis()}.${type.last()}")

        try {
            val outputStream = FileOutputStream(file)
            val inputStream = context.contentResolver.openInputStream(uri)

            inputStream.copyTo(outputStream, 1024)

            inputStream.close()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)
    }

}
