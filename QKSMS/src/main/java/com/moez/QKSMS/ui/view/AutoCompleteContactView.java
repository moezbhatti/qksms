package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.MultiAutoCompleteTextView;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.moez.QKSMS.common.FontManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.ThemeManager;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.ui.base.QKActivity;

public class AutoCompleteContactView extends RecipientEditTextView {
    public static final String TAG = "AutoCompleteContactView";

    private QKActivity mContext;
    private BaseRecipientAdapter mAdapter;

    public AutoCompleteContactView(Context context) {
        this(context, null);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public AutoCompleteContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context) {
        mContext = (QKActivity) context;

        mAdapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, getContext());

        setThreshold(1);
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        setAdapter(mAdapter);
        setOnItemClickListener(this);

        LiveViewManager.registerView(QKPreference.FONT_FAMILY, this, key -> {
            setTypeface(FontManager.getFont(mContext));
        });

        LiveViewManager.registerView(QKPreference.FONT_WEIGHT, this, key -> {
            setTypeface(FontManager.getFont(mContext));
        });

        LiveViewManager.registerView(QKPreference.FONT_SIZE, this, key -> {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, FontManager.getTextSize(FontManager.TEXT_TYPE_PRIMARY));
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            setHintTextColor(ThemeManager.getTextOnBackgroundSecondary());
        });

        LiveViewManager.registerView(QKPreference.MOBILE_ONLY, this, key -> {
            if (mAdapter != null) {
                mAdapter.setShowMobileOnly(QKPreferences.getBoolean(QKPreference.MOBILE_ONLY));
            }
        });
    }
}
