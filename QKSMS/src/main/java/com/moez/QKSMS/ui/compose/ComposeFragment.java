package com.moez.QKSMS.ui.compose;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.common.utils.PhoneNumberUtils;
import com.moez.QKSMS.interfaces.ActivityLauncher;
import com.moez.QKSMS.interfaces.RecipientProvider;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.ui.base.QKFragment;
import com.moez.QKSMS.ui.messagelist.MessageListActivity;
import com.moez.QKSMS.ui.view.AutoCompleteContactView;
import com.moez.QKSMS.ui.view.ComposeView;
import com.moez.QKSMS.ui.view.StarredContactsView;

public class ComposeFragment extends QKFragment implements ActivityLauncher, RecipientProvider,
        ComposeView.OnSendListener, AdapterView.OnItemClickListener {

    public static final String TAG = "ComposeFragment";

    private AutoCompleteContactView mRecipients;
    private ComposeView mComposeView;
    private StarredContactsView mStarredContactsView;

    public ComposeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        mRecipients = (AutoCompleteContactView) view.findViewById(R.id.compose_recipients);
        mRecipients.setOnItemClickListener(this);

        mComposeView = (ComposeView) view.findViewById(R.id.compose_view);
        mComposeView.onOpenConversation(null, null);
        mComposeView.setActivityLauncher(this);
        mComposeView.setRecipientProvider(this);
        mComposeView.setOnSendListener(this);
        mComposeView.setLabel("Compose");

        mStarredContactsView = (StarredContactsView) view.findViewById(R.id.starred_contacts);
        mStarredContactsView.setComposeScreenViews(mRecipients, mComposeView);

        new Handler().postDelayed(() -> KeyboardUtils.showAndFocus(mContext, mRecipients), 100);

        return view;
    }

    @Override
    public void onSend(String[] recipients, String body) {
        long threadId = Utils.getOrCreateThreadId(mContext, recipients[0]);
        if (threadId != 0) {
            mContext.finish();
            MessageListActivity.launch(mContext, threadId, -1, null, true);
        } else {
            mContext.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mComposeView != null) {
            mComposeView.saveDraft();
        }
    }

    /**
     * @return the addresses of all the contacts in the AutoCompleteContactsView.
     */
    @Override
    public String[] getRecipientAddresses() {
        DrawableRecipientChip[] chips = mRecipients.getRecipients();
        String[] addresses = new String[chips.length];

        for (int i = 0; i < chips.length; i++) {
            addresses[i] = PhoneNumberUtils.stripSeparators(chips[i].getEntry().getDestination());
        }

        return addresses;
    }

    /**
     * Photo Selection result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (!mComposeView.onActivityResult(requestCode, resultCode, data)) {
            // Wasn't handled by ComposeView
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mRecipients.onItemClick(parent, view, position, id);
        mStarredContactsView.collapse();
        mComposeView.requestReplyTextFocus();
    }

    public boolean isReplyTextEmpty() {
        if (mComposeView != null) {
            return mComposeView.isReplyTextEmpty();
        }
        return true;
    }
}
