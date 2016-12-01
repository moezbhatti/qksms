package com.moez.QKSMS.ui.attachmentlist;

import android.graphics.Bitmap;

/**
 * Created by barrus on 11/29/16.
 */

public class AttachmentItem {
    private int mPosition;
    private Bitmap mBitmap;
    private int mType;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public int getType(){
        return mType;
    }

    public void setType(int type){
        mType = type;
    }
}