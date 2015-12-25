package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class QKSwitch extends SwitchCompat implements LiveView {

    private Resources mRes;

    public QKSwitch(Context context) {
        super(context);
        init(context);
    }

    public QKSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mRes = context.getResources();

        // Register this view for live updates.
        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);

        refresh();
    }

    @Override
    public void refresh() {
        DrawableCompat.setTintList(getThumbDrawable(), getSwitchThumbColorStateList());
        DrawableCompat.setTintList(getTrackDrawable(), getSwitchTrackColorStateList());
    }

    private ColorStateList getSwitchThumbColorStateList() {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];

        // Disabled state
        states[0] = new int[]{-android.R.attr.state_enabled};
        colors[0] = mRes.getColor(ThemeManager.getInstance().isNightMode() ?
                R.color.switch_thumb_disabled_dark : R.color.switch_thumb_disabled_light);

        // Checked state
        states[1] = new int[]{android.R.attr.state_checked};
        colors[1] = ThemeManager.getInstance().getColor();

        // Unchecked enabled state state
        states[2] = new int[0];
        colors[2] = mRes.getColor(ThemeManager.getInstance().isNightMode() ?
                R.color.switch_thumb_enabled_dark : R.color.switch_thumb_enabled_light);

        return new ColorStateList(states, colors);
    }

    private ColorStateList getSwitchTrackColorStateList() {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];

        // Disabled state
        states[0] = new int[]{-android.R.attr.state_enabled};
        colors[0] = mRes.getColor(ThemeManager.getInstance().isNightMode() ?
                R.color.switch_track_disabled_dark : R.color.switch_track_disabled_light);

        // Checked state
        states[1] = new int[]{android.R.attr.state_checked};
        colors[1] = Color.argb(0x4D, // 30% alpha
                Color.red(ThemeManager.getInstance().getColor()),
                Color.green(ThemeManager.getInstance().getColor()),
                Color.blue(ThemeManager.getInstance().getColor()));

        // Unchecked enabled state state
        states[2] = new int[0];
        colors[2] = mRes.getColor(ThemeManager.getInstance().isNightMode() ?
                R.color.switch_track_enabled_dark : R.color.switch_track_enabled_light);

        return new ColorStateList(states, colors);
    }
}