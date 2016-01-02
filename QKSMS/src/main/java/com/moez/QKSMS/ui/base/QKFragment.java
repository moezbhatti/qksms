package com.moez.QKSMS.ui.base;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.preferences.QKPreference;
import com.moez.QKSMS.ui.ThemeManager;
import com.squareup.leakcanary.RefWatcher;

public class QKFragment extends Fragment {

    protected QKActivity mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (QKActivity) activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LiveViewManager.registerView(QKPreference.BACKGROUND, key -> {
            if (getView() != null) {
                getView().setBackgroundColor(ThemeManager.getBackgroundColor());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = QKSMSApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
