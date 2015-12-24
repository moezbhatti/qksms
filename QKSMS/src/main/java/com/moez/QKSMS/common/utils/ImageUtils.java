package com.moez.QKSMS.common.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap, int radius) {

        Bitmap sbmp;

        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius) {
            float smallest = Math.min(bitmap.getWidth(), bitmap.getHeight());
            float factor = smallest / radius;
            sbmp = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / factor), (int) (bitmap.getHeight() / factor), false);
        } else {
            sbmp = bitmap;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, radius, radius);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(radius / 2, radius / 2, radius / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

    public static RoundedBitmapDrawable getRoundedDrawable(Context context, Drawable drawable, int radiusDp) {
        return getRoundedDrawable(context, ((BitmapDrawable) drawable).getBitmap(), radiusDp);
    }

    public static RoundedBitmapDrawable getRoundedDrawable(Context context, Bitmap bitmap, int radiusDp) {
        float radius = (float) Units.dpToPx(context, radiusDp);

        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        drawable.setCornerRadius(radius);
        drawable.setAntiAlias(true);

        return drawable;
    }

    /**
     * Shrinks a bitmap so that, when it is encoded to a jpg with the given compression level, its
     * size is less than the given number of bytes.
     *
     * @param src The bitmap to shrink
     * @param jpgCompression The JPEG compression level to use when judging the size of the file
     * @param maxSize The maximum size that the compressed JPEG should be
     * @return The shrinked bitmap
     */
    public static Bitmap shrink(Bitmap src, int jpgCompression, long maxSize) {

        // Factor to scale the size down by each time
        final float factor = 0.5f;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, jpgCompression, stream);

        while (maxSize != -1 && stream.size() > maxSize) {
            // Calculate new height / width
            int height = (int) (src.getHeight() * factor);
            int width = (int) (src.getWidth() * factor);

            // Scale the image and compress to jpg again
            stream.reset();
            src = Bitmap.createScaledBitmap(src, width, height, false);
            src.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        }

        return src;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        final int result;
        Cursor cursor = context.getContentResolver().query(
                photoUri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null
        );
        if (cursor == null) {
            return 0;
        }
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        } else {
            result = 0;
        }
        cursor.close();
        return result;
    }
}
