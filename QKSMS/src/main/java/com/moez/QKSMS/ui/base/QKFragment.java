package com.moez.QKSMS.ui.base;

import android.app.Fragment;
import com.moez.QKSMS.QKSMSApp;
import com.squareup.leakcanary.RefWatcher;

public class QKFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = QKSMSApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
