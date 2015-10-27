package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.moez.QKSMS.common.FontManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.TypefaceManager;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class QKEditText extends android.widget.EditText implements LiveView {
    public static final String TAG = "QKEditText";
    private Context mContext;

    public QKEditText(Context context) {
        super(context);

        if (!isInEditMode()) {
            init(context, null);
        }
    }

    public QKEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public QKEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;

        // Load the properties
        refresh();

        setText(getText());

        // Register this view for live updates.
        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.FONT_FAMILY);
        LiveViewManager.registerPreference(this, SettingsFragment.FONT_SIZE);
        LiveViewManager.registerPreference(this, SettingsFragment.FONT_WEIGHT);
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text) || Build.VERSION.SDK_INT < 19) {
            text = new SpannableStringBuilder(text);
        }
        super.setText(text, type);
    }

    public void setTextChangedListener(final TextChangedListener listener) {
        if (listener != null) {
            addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    listener.onTextChanged(s);
                }
            });
        }
    }

    @Override
    public void refresh() {
        // Typeface and colors
        int fontFamily = FontManager.getFontFamily(mContext);
        int fontWeight = FontManager.getFontWeight(mContext, false);
        setTypeface(TypefaceManager.obtainTypeface(mContext, fontFamily, fontWeight,
                TypefaceManager.TextStyle.NORMAL));
        setTextColor(ThemeManager.getTextOnBackgroundPrimary());
        setHintTextColor(ThemeManager.getTextOnBackgroundSecondary());

        // Text size
        int sp = FontManager.getTextSize(mContext, FontManager.TEXT_TYPE_PRIMARY);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }

    public interface TextChangedListener {
        void onTextChanged(CharSequence s);
    }
}
