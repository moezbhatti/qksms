package com.moez.QKSMS.ui.compose;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.base.QKSwipeBackActivity;

public class ComposeActivity extends QKSwipeBackActivity {

    private ComposeFragment mComposeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_compose);

        FragmentManager fm = getFragmentManager();
        mComposeFragment = (ComposeFragment) fm.findFragmentById(R.id.content_frame);
        if (mComposeFragment == null) {
            mComposeFragment = new ComposeFragment();
        }

        fm.beginTransaction()
                .replace(R.id.content_frame, mComposeFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.compose, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
