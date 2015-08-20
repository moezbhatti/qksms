package com.moez.QKSMS.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class QKCheckBox extends android.widget.CheckBox implements LiveView {
    public static final String TAG = "QKCheckBox";

    private static final int ALPHA_ENABLED = 255;
    private static final int ALPHA_DISABLED = 128;

    private static final int ANIMATION_FRAME_START = 0;
    private static final int ANIMATION_FRAME_COMPLETE = 15;

    private static final int ANIMATION_DURATION = 200; // ms

    private Drawable mButtonDrawable;

    public QKCheckBox(Context context) { this(context, null); }
    public QKCheckBox(Context context, AttributeSet attrs) { this(context, attrs, android.R.attr.checkboxStyle); }
    public QKCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDrawable(ANIMATION_FRAME_COMPLETE);

        // This disables an annoying double ripple effect on Lollipop+ android versions.
        // But, we can't just always disable the background, because that makes the CheckBox
        // invisible for older Android versions (i.e. 4.1.2, perhaps older)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackground(null);
        }

        // Register this view for live updates.
        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);
    }

    /**
     * Sets the button drawable, updating the color of it according to the theme color.
     * @param drawable
     */
    @Override
    public void setButtonDrawable(Drawable drawable) {
        mButtonDrawable = drawable;
        mButtonDrawable.setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
        super.setButtonDrawable(drawable);
    }

    @Override
    public void refresh() {
        // Refresh the button drawable with the new theme color.
        setButtonDrawable(mButtonDrawable);
    }

    @Override
    public void setChecked(boolean checked) {
        boolean animate = checked != isChecked();
        super.setChecked(checked);

        if (animate) {
            ObjectAnimator checkBoxAnimator = ObjectAnimator.ofInt(
                    this, "drawable", ANIMATION_FRAME_START, ANIMATION_FRAME_COMPLETE
            );
            checkBoxAnimator.setDuration(ANIMATION_DURATION);
            checkBoxAnimator.start();
        }
    }

    private void setDrawable(int number) {
        // TODO: Preload the drawables as constants instead of loading them this way? Might be faster

        Resources res = getContext().getResources();
        // i.e. btn_check_to_off_mtrl_003, or btn_check_to_on_mtrl_013.
        // the "%03d" part just means "insert this number, padding it so that it's always three
        // digits"
        String name = String.format("btn_check_to_%s_mtrl_%03d", isChecked() ? "on" : "off", number);
        int id = res.getIdentifier(name, "drawable", getContext().getPackageName());

        try {
            setButtonDrawable(ContextCompat.getDrawable(getContext(), id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mButtonDrawable.mutate().setAlpha(isEnabled() ? ALPHA_ENABLED : ALPHA_DISABLED);
        super.onDraw(canvas);
    }
}
