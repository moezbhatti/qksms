package com.moez.QKSMS.ui.compose;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;

import com.moez.QKSMS.R;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.ui.base.QKSwipeBackActivity;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;

public class ComposeActivity extends QKSwipeBackActivity {

    private ComposeFragment mComposeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_compose);

        FragmentManager fm = getFragmentManager();
        mComposeFragment = (ComposeFragment) fm.findFragmentByTag(ComposeFragment.TAG);
        if (mComposeFragment == null) {
            mComposeFragment = new ComposeFragment();
        }

        fm.beginTransaction()
                .replace(R.id.content_frame, mComposeFragment, ComposeFragment.TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.compose, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        // Check if we're not the default SMS app
        if (!Utils.isDefaultSmsApp(this)) {
            // Ask to become the default SMS app
            new DefaultSmsHelper(this, R.string.not_default_send)
                    .showIfNotDefault((ViewGroup)getWindow().getDecorView().getRootView());
        } else if (mComposeFragment != null && !mComposeFragment.isReplyTextEmpty()
                && mComposeFragment.getRecipientAddresses().length == 0) {
            // If there is Draft message and no recipients are set
            new QKDialog()
                    .setContext(this)
                    .setMessage(R.string.discard_message_reason)
                    .setPositiveButton(R.string.yes, v -> {
                        super.onBackPressed();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
