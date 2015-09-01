package com.moez.QKSMS.ui.popup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.AdapterView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.interfaces.ActivityLauncher;
import com.moez.QKSMS.interfaces.RecipientProvider;
import com.moez.QKSMS.ui.base.QKPopupActivity;
import com.moez.QKSMS.ui.view.AutoCompleteContactView;
import com.moez.QKSMS.ui.view.ComposeView;
import com.moez.QKSMS.ui.view.StarredContactsView;

public class QKComposeActivity extends QKPopupActivity implements ComposeView.OnSendListener, RecipientProvider,
        ActivityLauncher, AdapterView.OnItemClickListener {

    private final String TAG = "QKComposeActivity";

    private Context mContext = this;
    private AutoCompleteContactView mRecipients;
    private StarredContactsView mStarredContactsView;
    private ComposeView mCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_compose);

        mRecipients = (AutoCompleteContactView) findViewById(R.id.compose_recipients);
        mRecipients.setOnItemClickListener(this);

        // Get the compose view, and set it up with all the listeners.
        findViewById(R.id.compose_view_stub).setVisibility(View.VISIBLE);
        mCompose = (ComposeView) findViewById(R.id.compose_view);
        mCompose.setActivityLauncher(this);
        mCompose.setOnSendListener(this);
        mCompose.setRecipientProvider(this);
        mCompose.setLabel("QKCompose");

        mStarredContactsView = (StarredContactsView) findViewById(R.id.starred_contacts);
        mStarredContactsView.setComposeScreenViews(mRecipients, mCompose);

        // Apply different attachments based on the type.
        mCompose.loadMessageFromIntent(getIntent());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_qkcompose;
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyboardUtils.hide(mContext, mCompose);
    }

    @Override
    public void finish() {
        // Override pending transitions so that we don't see black for a second when QuickReply closes
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public String[] getRecipientAddresses() {
        DrawableRecipientChip[] chips = mRecipients.getRecipients();
        String[] addresses = new String[chips.length];

        for (int i = 0; i < chips.length; i++) {
            addresses[i] = PhoneNumberUtils.stripSeparators(chips[i].getEntry().getDestination());
        }

        return addresses;
    }

    @Override
    public void onSend(String[] recipients, String body) {
        // When the SMS is sent, close this activity.
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!mCompose.onActivityResult(requestCode, resultCode, data)) {
            // Handle other results here, since the ComposeView didn't handle them.
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mRecipients.onItemClick(parent, view, position, id);
        mStarredContactsView.collapse();
        mCompose.requestReplyTextFocus();
    }
}
