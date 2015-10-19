package com.moez.QKSMS.ui.base;

import android.app.Fragment;
import android.os.Bundle;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.squareup.leakcanary.RefWatcher;

public class QKFragment extends Fragment implements LiveView {

    protected QKActivity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (QKActivity) getActivity();

        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = QKSMSApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void refresh() {
        if (getView() != null) {
            getView().setBackgroundColor(ThemeManager.getBackgroundColor());
        }
    }
}
