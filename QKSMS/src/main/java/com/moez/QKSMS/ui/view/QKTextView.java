package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.FontManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.ThemeManager;
import com.moez.QKSMS.common.utils.TextUtils;
import com.moez.QKSMS.enums.QKPreference;

public class QKTextView extends TextView {
    private final String TAG = "QKTextView";

    private Context mContext;
    private int mType = FontManager.TEXT_TYPE_PRIMARY;
    private boolean mOnColorBackground = false;

    public QKTextView(Context context) {
        super(context);

        if (!isInEditMode()) {
            init(context, null);
        }
    }

    public QKTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public QKTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;


        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.QKTextView);
            mType = array.getInt(R.styleable.QKTextView_type, FontManager.TEXT_TYPE_PRIMARY);
            array.recycle();
        }

        setTextColor(FontManager.getTextColor(mContext, mType));
        setText(getText());

        setType(mType);
    }

    public void setType(int type) {
        mType = type;

        // Register for theme updates if we're text that changes color dynamically.
        if (mType == FontManager.TEXT_TYPE_CATEGORY) {
            LiveViewManager.registerView(QKPreference.THEME, this, key ->
                    setTextColor(FontManager.getTextColor(mContext, mType)));
        }

        LiveViewManager.registerView(QKPreference.FONT_FAMILY, this, key -> {
            setTypeface(FontManager.getFont(mContext, type));
        });

        LiveViewManager.registerView(QKPreference.FONT_WEIGHT, this, key -> {
            setTypeface(FontManager.getFont(mContext, type));
        });

        LiveViewManager.registerView(QKPreference.FONT_SIZE, this, key -> {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, FontManager.getTextSize(mType));
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            setTextColor(FontManager.getTextColor(mContext, mType));
        });

        LiveViewManager.registerView(QKPreference.TEXT_FORMATTING, this, key -> {
            setText(getText(), BufferType.NORMAL);
        });
    }

    public void setOnColorBackground(boolean onColorBackground) {
        if (onColorBackground != mOnColorBackground) {
            mOnColorBackground = onColorBackground;

            if (onColorBackground) {
                if (mType == FontManager.TEXT_TYPE_PRIMARY) {
                    setTextColor(ThemeManager.getTextOnColorPrimary());
                    setLinkTextColor(ThemeManager.getTextOnColorPrimary());
                } else if (mType == FontManager.TEXT_TYPE_SECONDARY ||
                        mType == FontManager.TEXT_TYPE_TERTIARY) {
                    setTextColor(ThemeManager.getTextOnColorSecondary());
                }
            } else {
                if (mType == FontManager.TEXT_TYPE_PRIMARY) {
                    setTextColor(ThemeManager.getTextOnBackgroundPrimary());
                    setLinkTextColor(ThemeManager.getColor());
                } else if (mType == FontManager.TEXT_TYPE_SECONDARY ||
                        mType == FontManager.TEXT_TYPE_TERTIARY) {
                    setTextColor(ThemeManager.getTextOnBackgroundSecondary());
                }
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {

        if (mType == FontManager.TEXT_TYPE_DIALOG_BUTTON) {
            text = text.toString().toUpperCase();
        }

        if (QKPreferences.getBoolean(QKPreference.TEXT_FORMATTING)) {
            text = TextUtils.styleText(text);
            if (text == null || text.length() <= 0 || Build.VERSION.SDK_INT >= 19) {
                super.setText(text, BufferType.EDITABLE);
                return;
            }

            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            super.setText(builder, BufferType.EDITABLE);
        } else {
            super.setText(text, BufferType.NORMAL);
        }

    }
}
