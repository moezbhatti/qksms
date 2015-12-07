package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class QKSwitch extends SwitchCompat implements LiveView {

    public QKSwitch(Context context) {
        super(context);
        init();
    }

    public QKSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Register this view for live updates.
        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);

        refresh();
    }

    @Override
    public void refresh() {
        //getThumbDrawable().setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
        //getTrackDrawable().setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
    }
}