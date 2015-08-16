package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class RunnableToast implements Runnable {

    private Context mContext;
    private String mText;
    private int mDuration;

    public RunnableToast(Context context, String text, int duration) {
        mContext = context;
        mText = text;
        mDuration = duration;
    }

    public RunnableToast(Context context, @StringRes int id, int duration) {
        mContext = context;
        mText = context.getString(id);
        mDuration = duration;
    }

    @Override
    public void run() {
        Toast.makeText(mContext, mText, mDuration).show();
    }
}
