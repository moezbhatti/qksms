package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AttachmentImageView extends ImageView {
    private boolean mHasAttachment = false;

    public AttachmentImageView(Context context) {
        super(context);
    }

    public AttachmentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mHasAttachment = bm != null;
    }

    public boolean hasAttachment() {
        return mHasAttachment;
    }
}
