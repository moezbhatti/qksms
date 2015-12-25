package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class QKImageView extends ImageView implements LiveView {

    private static final String TAG = "QKImageView";
    private Drawable mDrawable;

    public QKImageView(Context context) {
        super(context);
        init();
    }

    public QKImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QKImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        // Register this view for live updates.
        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        // Have to set this as null to refresh
        super.setImageDrawable(null);
        mDrawable = drawable;

        if (mDrawable != null) {
            mDrawable.setColorFilter(ThemeManager.getInstance().getColor(), PorterDuff.Mode.SRC_ATOP);
            super.setImageDrawable(drawable);
        }
    }

    @Override
    public void refresh() {
        setImageDrawable(mDrawable);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
