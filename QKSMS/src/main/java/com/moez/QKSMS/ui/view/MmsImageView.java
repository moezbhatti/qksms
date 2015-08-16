package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.moez.QKSMS.common.utils.ImageUtils;

public class MmsImageView extends ImageView {
    private final String TAG = "MmsImageView";

    public MmsImageView(Context context) {
        super(context);
    }

    public MmsImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null) {
            int width = getWidth();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            if (bitmap != null) {
                float ratio = (float) width / (float) bitmap.getWidth();
                int height = (int) (bitmap.getHeight() * ratio);

                if (width > 0 && height > 0) {
                    super.setImageDrawable(ImageUtils.getRoundedDrawable(getContext(), Bitmap.createScaledBitmap(bitmap, width, height, true), 4));
                }
            }
        } else {
            super.setImageDrawable(drawable);
        }
    }
}