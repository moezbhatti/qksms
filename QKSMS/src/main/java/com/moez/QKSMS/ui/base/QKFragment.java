package com.moez.QKSMS.ui.base;

import android.app.Fragment;
import android.os.Bundle;
import com.moez.QKSMS.QKSMSApp;
import com.squareup.leakcanary.RefWatcher;

public class QKFragment extends Fragment {

    protected QKActivity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (QKActivity) getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = QKSMSApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
