package com.moez.QKSMS.ui;

import android.app.ActivityManager.TaskDescription;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.DonationManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.QKRateSnack;
import com.moez.QKSMS.common.google.DraftCache;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.receiver.IconColorReceiver;
import com.moez.QKSMS.service.DeleteOldMessagesService;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.conversationlist.ConversationListFragment;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.dialog.mms.MMSSetupFragment;
import com.moez.QKSMS.ui.messagelist.MessageListActivity;
import com.moez.QKSMS.ui.search.SearchActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.welcome.WelcomeActivity;
import org.ligi.snackengage.SnackEngage;
import org.ligi.snackengage.snacks.BaseSnack;

import java.util.Collection;


public class MainActivity extends QKActivity {
    private final String TAG = "MainActivity";

    public static long sThreadShowing;

    private static final int THREAD_LIST_QUERY_TOKEN = 1701;
    private static final int UNREAD_THREADS_QUERY_TOKEN = 1702;
    public static final int DELETE_CONVERSATION_TOKEN = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;

    public static final String MMS_SETUP_DONT_ASK_AGAIN = "mmsSetupDontAskAgain";

    @Bind(R.id.root) View mRoot;

    private ConversationListFragment mConversationList;

    /**
     * True if the mms setup fragment has been dismissed and we shouldn't show it anymore.
     */
    private final String KEY_MMS_SETUP_FRAGMENT_DISMISSED = "mmsSetupFragmentShown";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchWelcomeActivity();
        onNewIntent(getIntent());

        setContentView(R.layout.activity_fragment);
        setTitle(R.string.title_conversation_list);
        ButterKnife.bind(this);

        FragmentManager fm = getFragmentManager();
        mConversationList = (ConversationListFragment) fm.findFragmentByTag(ConversationListFragment.TAG);
        if (mConversationList == null) {
            mConversationList = new ConversationListFragment();
        }
        FragmentTransaction menuTransaction = fm.beginTransaction();
        menuTransaction.replace(R.id.content_frame, mConversationList, ConversationListFragment.TAG);
        menuTransaction.commit();

        showDialogIfNeeded(savedInstanceState);

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            // Update the background color. This code is important during the welcome screen setup, when the activity
            // in the ThemeManager isn't the MainActivity
            mRoot.setBackgroundColor(ThemeManager.getBackgroundColor());
        });

        //Adds a small/non intrusive snackbar that asks the user to rate the app
        SnackEngage.from(this).withSnack(new QKRateSnack().withDuration(BaseSnack.DURATION_LONG))
                .build().engageWhenAppropriate();

        DeleteOldMessagesService.setupAutoDeleteAlarm(this);
    }

    /**
     * Shows at most one dialog using the intent extras and the restored state of the activity.
     *
     * @param savedInstanceState restored state
     */
    private void showDialogIfNeeded(Bundle savedInstanceState) {
        // Check if the intent has the ICON_COLOR_CHANGED action; if so, show a new dialog.
        if (getIntent().getBooleanExtra(IconColorReceiver.EXTRA_ICON_COLOR_CHANGED, false)) {
            // Clear the flag in the intent so that the dialog doesn't show up anymore
            getIntent().putExtra(IconColorReceiver.EXTRA_ICON_COLOR_CHANGED, false);

            // Display a dialog showcasing the new icon!
            ImageView imageView = new ImageView(this);
            PackageManager manager = getPackageManager();
            try {
                ComponentInfo info = manager.getActivityInfo(getComponentName(), 0);
                imageView.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), info.getIconResource()));
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            new QKDialog()
                    .setContext(this)
                    .setTitle(getString(R.string.icon_ready))
                    .setMessage(R.string.icon_ready_message)
                    .setCustomView(imageView)
                    .setPositiveButton(R.string.okay, null)
                    .show();

            // Only show the MMS setup fragment if it hasn't already been dismissed
        } else if (!wasMmsSetupFragmentDismissed(savedInstanceState)) {
            beginMmsSetup();
        }
    }

    private boolean wasMmsSetupFragmentDismissed(Bundle savedInstanceState) {
        // It hasn't been dismissed if the saved instance state isn't initialized, or is initialized
        // but doesn't have the flag.
        return savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_MMS_SETUP_FRAGMENT_DISMISSED, false);
    }

    private void launchWelcomeActivity() {
        if (mPrefs.getBoolean(SettingsFragment.WELCOME_SEEN, false)) {
            // User has already seen the welcome screen
            return;
        }

        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(welcomeIntent, WelcomeActivity.WELCOME_REQUEST_CODE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();

        showBackButton(false);
        mConversationList.inflateToolbar(menu, inflater, this);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onKeyUp(KeyEvent.KEYCODE_BACK, null);
                return true;
            case R.id.menu_search:
                startActivity(SearchActivity.class);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WelcomeActivity.WELCOME_REQUEST_CODE) {
            new DefaultSmsHelper(this, R.string.not_default_first).showIfNotDefault(null);
        }
    }

    public static Intent createAddContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        if (SmsHelper.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        return intent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DonationManager.getInstance(this).destroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mConversationList.isShowingBlocked()) {
                mConversationList.setShowingBlocked(false);
            } else {
                finish();
            }
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sThreadShowing = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Only mark screen if the screen is on. onStart() is still called if the app is in the
        // foreground and the screen is off
        // TODO this solution doesn't work if the activity is in the foreground but the lockscreen is on
        if (isScreenOn()) {
            SmsHelper.markSmsSeen(this);
            SmsHelper.markMmsSeen(this);
            NotificationManager.update(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        sThreadShowing = 0;

        NotificationManager.initQuickCompose(this, false, false);
    }

    /**
     * MainActivity has a "singleTask" launch mode, which means that if it is currently running
     * and another intent is launched to open it, instead of creating a new MainActivity it
     * just opens the current MainActivity. We use this so that when you click on notifications,
     * only one main activity is ever used.
     * <p>
     * onNewIntent() is called every time the homescreen shortcut is tapped, even if the app
     * is already running in the background. It's also called when the app is launched via other
     * intents
     * <p>
     * Docs:
     * http://developer.android.com/guide/components/tasks-and-back-stack.html#TaskLaunchModes
     */
    @Override
    public void onNewIntent(Intent intent) {
        // onNewIntent doesn't change the result of getIntent() by default, so here we set it since
        // that makes the most sense.
        setIntent(intent);

        boolean shouldOpenConversation = intent.hasExtra(MessageListActivity.ARG_THREAD_ID);

        // The activity can also be launched by clicking on the message button from the contacts app
        // Check for {sms,mms}{,to}: schemes, in which case we know to open a conversation
        if (intent.getData() != null) {
            String scheme = intent.getData().getScheme();
            shouldOpenConversation = shouldOpenConversation || scheme.startsWith("sms") || scheme.startsWith("mms");
        }

        if (shouldOpenConversation) {
            intent.setClass(this, MessageListActivity.class);
            startActivity(intent);
        }
    }

    private void beginMmsSetup() {
        if (!mPrefs.getBoolean(MMS_SETUP_DONT_ASK_AGAIN, false) &&
                TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMSC_URL, "")) &&
                TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMS_PROXY, "")) &&
                TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMS_PORT, ""))) {

            // Launch the MMS setup fragment here. This is a series of dialogs that will guide the
            // user through the MMS setup process.
            FragmentManager manager = getFragmentManager();
            if (manager.findFragmentByTag(MMSSetupFragment.TAG) == null) {
                MMSSetupFragment f = new MMSSetupFragment();
                Bundle args = new Bundle();
                args.putBoolean(MMSSetupFragment.ARG_ASK_FIRST, true);
                args.putString(MMSSetupFragment.ARG_DONT_ASK_AGAIN_PREF, MMS_SETUP_DONT_ASK_AGAIN);
                f.setArguments(args);

                getFragmentManager()
                        .beginTransaction()
                        .add(f, MMSSetupFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager m = getFragmentManager();

        // Save whether or not the mms setup fragment was dismissed
        if (m.findFragmentByTag(MMSSetupFragment.TAG) == null) {
            outState.putBoolean(KEY_MMS_SETUP_FRAGMENT_DISMISSED, true);
        }
    }

    /**
     * Build and show the proper delete thread dialog. The UI is slightly different
     * depending on whether there are locked messages in the thread(s) and whether we're
     * deleting single/multiple threads or all threads.
     *
     * @param listener          gets called when the delete button is pressed
     * @param threadIds         the thread IDs to be deleted (pass null for all threads)
     * @param hasLockedMessages whether the thread(s) contain locked messages
     * @param context           used to load the various UI elements
     */
    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener, Collection<Long> threadIds,
                                                 boolean hasLockedMessages, Context context) {
        View contents = View.inflate(context, R.layout.dialog_delete_thread, null);
        android.widget.TextView msg = (android.widget.TextView) contents.findViewById(R.id.message);

        if (threadIds == null) {
            msg.setText(R.string.confirm_delete_all_conversations);
        } else {
            // Show the number of threads getting deleted in the confirmation dialog.
            int cnt = threadIds.size();
            msg.setText(context.getResources().getQuantityString(
                    R.plurals.confirm_delete_conversation, cnt, cnt));
        }

        final CheckBox checkbox = (CheckBox) contents.findViewById(R.id.delete_locked);
        if (!hasLockedMessages) {
            checkbox.setVisibility(View.GONE);
        } else {
            listener.setDeleteLockedMessage(checkbox.isChecked());
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.setDeleteLockedMessage(checkbox.isChecked());
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_dialog_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(true)
                .setPositiveButton(R.string.delete, listener)
                .setNegativeButton(R.string.cancel, null)
                .setView(contents)
                .show();
    }

    public static class DeleteThreadListener implements DialogInterface.OnClickListener {
        private final Collection<Long> mThreadIds;
        private final Conversation.ConversationQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteLockedMessages;

        public DeleteThreadListener(Collection<Long> threadIds, Conversation.ConversationQueryHandler handler, Context context) {
            mThreadIds = threadIds;
            mHandler = handler;
            mContext = context;
        }

        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            mDeleteLockedMessages = deleteLockedMessages;
        }

        @Override
        public void onClick(DialogInterface dialog, final int whichButton) {
            MessageUtils.handleReadReport(mContext, mThreadIds,
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, () -> {
                        int token = DELETE_CONVERSATION_TOKEN;
                        if (mThreadIds == null) {
                            Conversation.startDeleteAll(mHandler, token, mDeleteLockedMessages);
                            DraftCache.getInstance().refresh();
                        } else {
                            Conversation.startDelete(mHandler, token, mDeleteLockedMessages, mThreadIds);
                        }
                    }
            );
            dialog.dismiss();
        }
    }
}
