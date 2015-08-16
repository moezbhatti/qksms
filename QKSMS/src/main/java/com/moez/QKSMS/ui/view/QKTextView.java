package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.common.FontManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.RobotoTypefaceManager;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class QKTextView extends TextView implements LiveView {
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
            for (int i = 0; i < attrs.getAttributeCount(); i++) {
                if (attrs.getAttributeName(i).equals("type")) {
                    mType = Integer.decode(attrs.getAttributeValue(i));
                    break;
                }
            }
        }

        refresh();
        setText(getText());

        // Register this view for live updates.
        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.FONT_FAMILY);
        LiveViewManager.registerPreference(this, SettingsFragment.FONT_SIZE);
        LiveViewManager.registerPreference(this, SettingsFragment.FONT_WEIGHT);
        LiveViewManager.registerPreference(this, SettingsFragment.MARKDOWN_ENABLED);
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);

        // Register for theme updates if we're text that changes color dynamically.
        if (mType == FontManager.TEXT_TYPE_CATEGORY) {
            LiveViewManager.registerPreference(this, SettingsFragment.THEME);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specModeW = MeasureSpec.getMode(widthMeasureSpec);
        if (specModeW != MeasureSpec.EXACTLY) {
            Layout layout = getLayout();
            int linesCount = layout.getLineCount();
            if (linesCount > 1) {
                float textRealMaxWidth = 0;
                for (int n = 0; n < linesCount; ++n) {
                    textRealMaxWidth = Math.max(textRealMaxWidth, layout.getLineWidth(n));
                }
                int w = Math.round(textRealMaxWidth);
                if (w < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                            heightMeasureSpec);
                }
            }
        }

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

    public void setType(int type) {
        mType = type;
        refresh();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {

        SharedPreferences prefs = MainActivity.getPrefs(getContext());

        if (mType == FontManager.TEXT_TYPE_DIALOG_BUTTON) {
            text = text.toString().toUpperCase();
        }

        if (prefs.getBoolean(SettingsFragment.MARKDOWN_ENABLED, false)) {
            text = MessageUtils.styleText(text);
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

    /**
     * refresh() is called whenever a user preference around text size, font family, etc. has been
     * updated and the view needs to refresh its properties.
     */
    @Override
    public void refresh() {
        // Typeface
        int fontFamily = FontManager.getFontFamily(mContext);
        int fontWeight = FontManager.getFontWeight(mContext, FontManager.getIsFontHeavy(mType));
        setTypeface(RobotoTypefaceManager.obtainTypeface(mContext, fontFamily, fontWeight,
                RobotoTypefaceManager.TextStyle.NORMAL));

        // Text size and color
        setTextSize(TypedValue.COMPLEX_UNIT_SP, FontManager.getTextSize(mContext, mType));
        setTextColor(FontManager.getTextColor(mContext, mType));

        // Markdown support enabled
        setText(getText(), BufferType.NORMAL);
    }
}