package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.TypefaceManager;

public class RobotoTextView extends AppCompatTextView {

    public RobotoTextView(Context context) {
        this(context, null);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            initTypeface(this, context, attrs);
        }
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            initTypeface(this, context, attrs);
        }
    }

    private void initTypeface(TextView textView, Context context, AttributeSet attrs) {
        Typeface typeface = null;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);

            if (a.hasValue(R.styleable.RobotoTextView_typeface)) {
                int typefaceValue = a.getInt(R.styleable.RobotoTextView_typeface, TypefaceManager.Typefaces.ROBOTO_REGULAR);
                typeface = TypefaceManager.obtainTypeface(context, typefaceValue);
            }

            a.recycle();
        }

        if (typeface == null) {
            typeface = TypefaceManager.obtainTypeface(context, TypefaceManager.Typefaces.ROBOTO_REGULAR);
        }

        textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textView.setTypeface(typeface);
    }

}
