package repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.support.media.ExifInterface
import util.tryOrNull
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
        val bitmap = tryOrNull { MediaStore.Images.Media.getBitmap(context.contentResolver, uri) }

        var title = ""
        var description = ""

        when (uri.scheme) {
            "file" -> title = uri.lastPathSegment

            "content" -> {
                val projection = arrayOf(Images.Media.TITLE, Images.Media.DESCRIPTION)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        title = cursor.getString(0)
                        description = cursor.getString(1)
                    }
                }
            }
        }

        insertImage(context.contentResolver, bitmap, title, description)
    }

    /**
     * Modified from [android.provider.MediaStore.Images.Media.insertImage] to add date fields so
     * that the saved image is put at the start of the gallery
     */
    private fun insertImage(cr: ContentResolver, source: Bitmap?, title: String, description: String): String? {

        val values = ContentValues()
        values.put(Images.Media.TITLE, title)
        values.put(Images.Media.DISPLAY_NAME, title)
        values.put(Images.Media.DESCRIPTION, description)
        values.put(Images.Media.MIME_TYPE, "image/jpeg")

        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())

        var uri: Uri? = null

        try {
            uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            if (source != null) {
                cr.openOutputStream(uri).use { imageOut ->
                    source.compress(Bitmap.CompressFormat.JPEG, 100, imageOut)
                }

                val id = ContentUris.parseId(uri)
                // Wait until MINI_KIND thumbnail is generated.
                val miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null)
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50f, 50f, Images.Thumbnails.MICRO_KIND)
            } else {
                cr.delete(uri, null, null)
                uri = null
            }
        } catch (e: Exception) {
            if (uri != null) {
                cr.delete(uri, null, null)
                uri = null
            }
        }

        return uri?.toString()
    }

    /**
     * Modified from [android.provider.MediaStore.Images.Media.insertImage] to add date fields so
     * that the saved image is put at the start of the gallery
     */
    private fun storeThumbnail(cr: ContentResolver, source: Bitmap, id: Long, width: Float, height: Float, kind: Int): Bitmap? {
        val matrix = Matrix()

        val scaleX = width / source.width
        val scaleY = height / source.height

        matrix.setScale(scaleX, scaleY)

        val thumb = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

        val values = ContentValues(4)
        values.put(Images.Thumbnails.KIND, kind)
        values.put(Images.Thumbnails.IMAGE_ID, id.toInt())
        values.put(Images.Thumbnails.HEIGHT, thumb.height)
        values.put(Images.Thumbnails.WIDTH, thumb.width)

        val url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values)

        return tryOrNull {
            val thumbOut = cr.openOutputStream(url)
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut)
            thumbOut.close()
            thumb
        }
    }

}
