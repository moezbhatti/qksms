package com.moez.QKSMS.ui.compose;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.QKContentFragment;
import com.moez.QKSMS.ui.view.AutoCompleteContactView;
import com.moez.QKSMS.ui.view.ComposeView;
import com.moez.QKSMS.ui.view.StarredContactsView;

public class ComposeFragment extends QKContentFragment implements ActivityLauncher, RecipientProvider,
        ComposeView.OnSendListener, AdapterView.OnItemClickListener {

    private final String TAG = "ComposeFragment";

    /**
     * Set to true in the bundle if the ComposeFragment should show the keyboard. Defaults to false.
     */
    public static final String ARG_SHOW_KEYBOARD = "showKeyboard";

    /**
     * Set a FOCUS string to indicate where the focus should be for the keyboard. Defaults to
     * FOCUS_NOTHING.
     */
    public static final String ARG_FOCUS = "focus";

    public static final String FOCUS_NOTHING = "nothing";
    public static final String FOCUS_RECIPIENTS = "recipients";
    public static final String FOCUS_REPLY = "reply";

    private AutoCompleteContactView mRecipients;
    private ComposeView mComposeView;
    private StarredContactsView mStarredContactsView;

    // True if the fragment's arguments have changed, and we need to potentially perform a focus
    // operation when the fragment opens.
    private boolean mPendingFocus = false;

    /**
     * Returns a new ComposeFragment, configured with the args.
     *
     * @param args A Bundle with options for configuring this fragment. See the ARG_ constants for
     *             configuration options.
     * @return the new ComposeFragment
     */
    public static ComposeFragment getInstance(Bundle args) {
        return getInstance(args, null);
    }

    /**
     * Returns a ComposeFragment, configured with the args. If possible, the given fragment
     * is used instead of creating a new ComposeFragment.
     *
     * @param args          A Bundle with options for configuring this fragment. See the ARG_ constants for
     *                      configuration options.
     * @param reuseFragment A fragment that can be used instead of creating a new one.
     * @return the ComposeFragment, which may be recycled
     */
    public static ComposeFragment getInstance(Bundle args, Fragment reuseFragment) {
        ComposeFragment f;

        // Check if we can reuse the passed fragment.
        if (reuseFragment != null && reuseFragment instanceof ComposeFragment) {
            f = (ComposeFragment) reuseFragment;
        } else {
            f = new ComposeFragment();
        }

        // Set the arguments in this fragment.
        f.updateArguments(args);

        return f;
    }

    @Override
    public void onNewArguments() {
        // Set pending focus, because the new configuration means that we may need to focus.
        mPendingFocus = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        mRecipients = (AutoCompleteContactView) view.findViewById(R.id.compose_recipients);
        mRecipients.setOnItemClickListener(this);

        view.findViewById(R.id.compose_view_stub).setVisibility(View.VISIBLE);
        mComposeView = (ComposeView) view.findViewById(R.id.compose_view);
        mComposeView.onOpenConversation(null, null);
        mComposeView.setActivityLauncher(this);
        mComposeView.setRecipientProvider(this);
        mComposeView.setOnSendListener(this);
        mComposeView.setLabel("Compose");

        mStarredContactsView = (StarredContactsView) view.findViewById(R.id.starred_contacts);
        mStarredContactsView.setComposeScreenViews(mRecipients, mComposeView);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean handledByComposeView = mComposeView.onActivityResult(requestCode, resultCode, data);
        if (!handledByComposeView) {
            // ...
        }
    }

    @Override
    public void onSend(String[] recipients, String body) {
        long threadId = Utils.getOrCreateThreadId(mContext, recipients[0]);
        if (recipients.length == 1) {
            ((MainActivity) mContext).setConversation(threadId);
        } else {
            ((MainActivity) mContext).showMenu();
        }
    }

    @Override
    public void onContentOpened() {
        setupInput();
    }

    /**
     * Shows the keyboard and focuses on the recipients text view.
     */
    public void setupInput() {
        Bundle args = getArguments() == null ? new Bundle() : getArguments();

        if (mPendingFocus) {
            if (args.getBoolean(ARG_SHOW_KEYBOARD, false)) {
                KeyboardUtils.show(mContext);
            }

            String focus = args.getString(ARG_FOCUS, FOCUS_NOTHING);
            if (FOCUS_RECIPIENTS.equals(focus)) {
                mRecipients.requestFocus();
            } else if (FOCUS_REPLY.equals(focus)) {
                mComposeView.requestReplyTextFocus();
            }
        }
    }

    @Override
    public void onContentClosing() {
        // Clear the focus from this fragment.
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            getActivity().getCurrentFocus().clearFocus();
        }
    }

    @Override
    public void onContentClosed() {
        super.onContentClosed();
        if (mComposeView != null) {
            mComposeView.saveDraft();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mComposeView != null) {
            mComposeView.saveDraft();
        }
    }

    @Override
    public void onMenuChanging(float percentOpen) {

    }

    @Override
    public void inflateToolbar(Menu menu, MenuInflater inflater, Context context) {
        inflater.inflate(R.menu.compose, menu);
        ((QKActivity) context).setTitle(R.string.title_compose);

        super.onCreateOptionsMenu(menu, inflater);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mRecipients.onItemClick(parent, view, position, id);
        mStarredContactsView.collapse();
        mComposeView.requestReplyTextFocus();
    }
}
