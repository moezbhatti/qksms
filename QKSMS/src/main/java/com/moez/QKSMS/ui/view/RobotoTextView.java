package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.moez.QKSMS.common.RobotoTextViewUtils;

public class RobotoTextView extends AppCompatTextView {

    public RobotoTextView(Context context) {
        this(context, null);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            RobotoTextViewUtils.initTypeface(this, context, attrs);
        }
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            RobotoTextViewUtils.initTypeface(this, context, attrs);
        }
    }

}
