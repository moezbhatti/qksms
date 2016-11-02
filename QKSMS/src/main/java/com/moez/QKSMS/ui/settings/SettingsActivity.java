package com.moez.QKSMS.ui.settings;

import android.app.FragmentManager;
import android.os.Bundle;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.base.QKActivity;

public class SettingsActivity extends QKActivity {

    public static final String ARG_SETTINGS_PAGE = "settings_page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        showBackButton(true);

        FragmentManager fm = getFragmentManager();
        SettingsFragment settingsFragment = (SettingsFragment) fm.findFragmentByTag(SettingsFragment.TAG);
        if (settingsFragment == null) {
            int page = getIntent().getIntExtra(ARG_SETTINGS_PAGE, R.xml.settings_main);
            settingsFragment = SettingsFragment.newInstance(page);
            fm.beginTransaction()
                    .replace(R.id.content_frame, settingsFragment, SettingsFragment.TAG)
                    .commit();
        } else {
            fm.beginTransaction()
                    .show(settingsFragment)
                    .commit();
        }
    }
}
