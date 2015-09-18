package com.moez.QKSMS.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.DonationManager;
import com.moez.QKSMS.common.google.DraftCache;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.receiver.IconColorReceiver;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.QKContentFragment;
import com.moez.QKSMS.ui.compose.ComposeFragment;
import com.moez.QKSMS.ui.conversationlist.ConversationListFragment;
import com.moez.QKSMS.ui.dialog.ConversationNotificationSettingsDialog;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.dialog.mms.MMSSetupFragment;
import com.moez.QKSMS.ui.messagelist.MessageListFragment;
import com.moez.QKSMS.ui.search.SearchFragment;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.QKCheckBox;
import com.moez.QKSMS.ui.view.slidingmenu.SlidingMenu;
import com.moez.QKSMS.ui.welcome.WelcomeActivity;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.net.URLDecoder;
import java.util.Collection;

public class MainActivity extends QKActivity implements SlidingMenu.OnOpenListener, SlidingMenu.OnCloseListener,
        SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener, ActionClickListener {

    private final String TAG = "MainActivity";

    public final static String EXTRA_THREAD_ID = "thread_id";

    private final String KEY_TYPE = "type";
    private final String KEY_POSITION = "position";
    private final String KEY_THREADID = "thread_id";

    private final int TYPE_COMPOSE = 0;
    private final int TYPE_CONVERSATION = 1;
    private final int TYPE_SETTINGS = 2;
    private final int TYPE_SEARCH = 3;

    private static final int THREAD_LIST_QUERY_TOKEN = 1701;
    private static final int UNREAD_THREADS_QUERY_TOKEN = 1702;
    public static final int DELETE_CONVERSATION_TOKEN = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;

    public static final String MMS_SETUP_DONT_ASK_AGAIN = "mmsSetupDontAskAgain";

    // thread IDs are always nonnegative
    public static long threadId = 0;

    public static boolean isShowing = false;
    public static boolean isContentHidden = true;
    private static Resources res;
    private static SharedPreferences prefs;

    private SlidingMenu mSlidingMenu;
    private ConversationListFragment menuFragment;
    private Fragment content;
    private long mWaitingForThreadId = -1;

    private boolean mIsDestroyed = false;

    /**
     * True if the mms setup fragment has been dismissed and we shouldn't show it anymore.
     */
    private final String KEY_MMS_SETUP_FRAGMENT_DISMISSED = "mmsSetupFragmentShown";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPrefs(this);
        getRes(this);

        launchWelcomeActivity();

        setContentView(R.layout.activity_main);
        setTitle(R.string.title_conversation_list);

        mSlidingMenu = (SlidingMenu) findViewById(R.id.sliding_menu);
        setupSlidingMenu();

        setupFragments(savedInstanceState);
        onNewIntent(getIntent());

        showDialogIfNeeded(savedInstanceState);
    }

    /**
     * Sets up menu and content fragments
     * If the fragments are stored in the bundle, use those. Otherwise, instantiate new ones
     */
    private void setupFragments(Bundle savedInstanceState) {
        int type = 0;
        int position = 0;
        threadId = 0;

        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(KEY_TYPE, 0);
            position = savedInstanceState.getInt(KEY_POSITION, 0);
            threadId = savedInstanceState.getLong(KEY_THREADID, 0);
        }

        menuFragment = new ConversationListFragment();
        menuFragment.setPosition(position);
        getFragmentManager().beginTransaction()
                .replace(R.id.menu_frame, menuFragment)
                .commit();

        switch (type) {
            case TYPE_COMPOSE:
                content = new ComposeFragment();
                break;
            case TYPE_CONVERSATION:
                Bundle args = new Bundle();
                args.putLong(MessageListFragment.ARG_THREAD_ID, threadId);
                content = MessageListFragment.getInstance(args);
                break;
            case TYPE_SETTINGS:
                content = SettingsFragment.newInstance(R.xml.settings_simple);
                break;
            case TYPE_SEARCH:
                content = new SearchFragment();
                break;
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, content)
                .commit();
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
                    .show(getFragmentManager(), null);

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
        if (prefs.getBoolean(SettingsFragment.WELCOME_SEEN, false)) {
            // User has already seen the welcome screen
            return;
        }

        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(welcomeIntent, WelcomeActivity.WELCOME_REQUEST_CODE);
    }

    public SlidingMenu getSlidingMenu() {
        return mSlidingMenu;
    }

    public void showMenu() {
        mSlidingMenu.showMenu();
    }

    private void setupSlidingMenu() {
        setSlidingTabEnabled(prefs.getBoolean(SettingsFragment.SLIDING_TAB, false));
        mSlidingMenu.setBehindScrollScale(0.5f);
        mSlidingMenu.setFadeDegree(0.5f);
        mSlidingMenu.setOnOpenListener(this);
        mSlidingMenu.setOnCloseListener(this);
        mSlidingMenu.setOnOpenedListener(this);
        mSlidingMenu.setOnClosedListener(this);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow_slidingmenu);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setContent(R.layout.content_frame);
        mSlidingMenu.setMenu(R.layout.menu_frame);
    }

    /**
     * Configured the sliding menu view to peek the content or not.
     *
     * @param slidingTabEnabled true to peek the content
     */
    public void setSlidingTabEnabled(boolean slidingTabEnabled) {
        if (slidingTabEnabled) {
            mSlidingMenu.setShadowDrawable(R.drawable.shadow_slidingmenu);
            mSlidingMenu.setShadowWidth(Units.dpToPx(this, 8));
            mSlidingMenu.setBehindOffset(Units.dpToPx(this, 48));
        } else {
            mSlidingMenu.setShadowWidth(0);
            mSlidingMenu.setBehindOffset(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (mSlidingMenu.isMenuShowing() || content == null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            setTitle(this.menuFragment.getTitleId());
            inflater.inflate(R.menu.coversation_list, menu);

        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (content instanceof MessageListFragment) {
                inflater.inflate(R.menu.conversation, menu);
                ((MessageListFragment) content).setTitle();

            } else if (content instanceof SearchFragment) {
                setTitle(getString(R.string.title_search));
                inflater.inflate(R.menu.search, menu);

            } else if (content instanceof SettingsFragment) {
                setTitle(getString(R.string.title_settings));
                inflater.inflate(R.menu.settings, menu);
                MenuItem simplePrefs = menu.findItem(R.id.simple_settings);
                if (prefs.getBoolean(SettingsFragment.SIMPLE_PREFS, true)) {
                    simplePrefs.setTitle(R.string.menu_show_all_prefs);
                } else {
                    simplePrefs.setTitle(R.string.menu_show_fewer_prefs);
                }

            } else if (content instanceof ComposeFragment) {
                setTitle(getString(R.string.title_compose));
                inflater.inflate(R.menu.compose, menu);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    public Fragment getMenuFragment() {
        return menuFragment;
    }

    public Fragment getContent() {
        return content;
    }

    public long getThreadId() {
        return threadId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onKeyUp(KeyEvent.KEYCODE_BACK, null);
                break;
            case R.id.simple_settings:
                prefs.edit().putBoolean(SettingsFragment.SIMPLE_PREFS,
                        !prefs.getBoolean(SettingsFragment.SIMPLE_PREFS, true)).apply();
            case R.id.menu_settings:
                switchContent(SettingsFragment.newInstance(R.xml.settings_simple), true);
                break;
            case R.id.menu_search:
                switchContent(new SearchFragment(), true);
                break;
            case R.id.menu_changelog:
                new QKDialog()
                        .setContext(this)
                        .setTitle(R.string.title_changelog)
                        .setTripleLineItems(R.array.changelog_versions, R.array.changelog_dates, R.array.changelog_changes, null)
                        .show(getFragmentManager(), "Changelog");
                break;
            case R.id.menu_donate:
                DonationManager.getInstance(this).showDonateDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getResultForThreadId(long threadId) {
        mWaitingForThreadId = threadId;
    }

    /**
     * When In-App billing is done, it'll return information via onActivityResult().
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConversationNotificationSettingsDialog.RINGTONE_REQUEST_CODE) {
            if (data != null) {
                if (mWaitingForThreadId > 0) {
                    ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(this, mWaitingForThreadId);
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    conversationPrefs.putString(SettingsFragment.NOTIFICATION_TONE, uri.toString());
                    mWaitingForThreadId = -1;
                }
            }

        } else if (requestCode == WelcomeActivity.WELCOME_REQUEST_CODE) {
            new DefaultSmsHelper(this, null, R.string.not_default_first).showIfNotDefault(null);
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
        mIsDestroyed = true;
        DonationManager.getInstance(this).destroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mSlidingMenu.isMenuShowing()) {
                Fragment category = getFragmentManager().findFragmentByTag(SettingsFragment.CATEGORY_TAG);
                if (category != null) {
                    getFragmentManager().beginTransaction().remove(category).commit();
                } else {
                    mSlidingMenu.showMenu();
                }
                return true;
            } else {
                if (menuFragment.isShowingBlocked()) {
                    menuFragment.setShowingBlocked(false);
                } else {
                    finish();
                }
            }
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowing = false;
        if (!mSlidingMenu.isMenuShowing()) {
            QKContentFragment.notifyOnContentClosed(content);
        }
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
        isShowing = true;
        ThemeManager.loadThemeProperties(this);

        if (!mSlidingMenu.isMenuShowing()) {
            QKContentFragment.notifyOnContentOpened(content);
        }

        NotificationManager.initQuickCompose(this, false, false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        // MainActivity has a "singleTask" launch mode, which means that if it is currently running
        // and another intent is launched to open it, instead of creating a new MainActivity it
        // just opens the current MainActivity. We use this so that when you click on notifications,
        // only one main activity is ever used.
        //
        // Docs:
        // http://developer.android.com/guide/components/tasks-and-back-stack.html#TaskLaunchModes

        // onNewIntent doesn't change the result of getIntent() by default, so here we set it since
        // that makes the most sense.
        setIntent(intent);

        // This method is called whenever a MainActivity intent is started. Sometimes this is from a
        // notification; other times it's from the user clicking on the app icon in the home screen
        long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);

        // The activity can also be launched by clicking on the message button from the contacts app
        // Check for {sms,mms}{,to}: schemes, in which case we know to open a conversation
        if (intent.getData() != null) {
            String data = intent.getData().toString();
            String scheme = intent.getData().getScheme();

            if (scheme.startsWith("smsto") || scheme.startsWith("mmsto")) {
                String address = data.replace("smsto:", "").replace("mmsto:", "");
                threadId = Utils.getThreadId(this, formatPhoneNumber(address));
            } else if (scheme.startsWith("sms") || (scheme.startsWith("mms"))) {
                String address = data.replace("sms:", "").replace("mms:", "");
                threadId = Utils.getThreadId(this, formatPhoneNumber(address));
            }
        }

        // If it has a thread id, then we know it's from a notification and we can set the
        // conversation.
        if (threadId != -1) {
            Log.v(TAG, "Opening thread: " + threadId);
            setConversation(threadId);
            mSlidingMenu.showContent();
        } else {
            mSlidingMenu.showMenu(false);
        }

        // Otherwise we'll just resume what was previously there, which doesn't require any code.
    }

    private String formatPhoneNumber(String address) {
        address = URLDecoder.decode(address);
        address = "" + Html.fromHtml(address);
        address = PhoneNumberUtils.formatNumber(address);
        return address;
    }

    private void beginMmsSetup() {
        if (!prefs.getBoolean(MMS_SETUP_DONT_ASK_AGAIN, false) &&
                TextUtils.isEmpty(prefs.getString(SettingsFragment.MMSC_URL, "")) &&
                TextUtils.isEmpty(prefs.getString(SettingsFragment.MMS_PROXY, "")) &&
                TextUtils.isEmpty(prefs.getString(SettingsFragment.MMS_PORT, ""))) {

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

        outState.putInt(KEY_TYPE, content instanceof MessageListFragment ? TYPE_CONVERSATION :
                content instanceof SettingsFragment ? TYPE_SETTINGS :
                        content instanceof SearchFragment ? TYPE_SEARCH : TYPE_COMPOSE);
        outState.putInt(KEY_POSITION, menuFragment.getPosition());
        outState.putLong(KEY_THREADID, threadId);
    }

    public void switchContent(Fragment fragment, boolean animate) {
        // Make sure that the activity isn't destroyed before making fragment transactions.
        if (fragment != null && !mIsDestroyed) {
            KeyboardUtils.hide(this);

            content = fragment;
            FragmentManager m = getFragmentManager();

            // Only do a replace if it is a different fragment.
            if (fragment != m.findFragmentById(R.id.content_frame)) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commitAllowingStateLoss();
            }

            mSlidingMenu.showContent(animate);
            invalidateOptionsMenu();

        } else {
            Log.w(TAG, "Null fragment, can't switch content");
        }
    }

    public void setConversation(long threadId) {
        setConversation(threadId, -1, null, false);
    }

    public void setConversation(long threadId, long rowId) {
        setConversation(threadId, rowId, null, false);
    }

    public void setConversation(long threadId, long rowId, String pattern) {
        setConversation(threadId, rowId, pattern, false);
    }

    public void setConversation(long threadId, long rowId, String pattern, boolean animate) {

        // Build the arguments for this conversation
        Bundle args = new Bundle();
        args.putLong(MessageListFragment.ARG_THREAD_ID, threadId);
        args.putLong(MessageListFragment.ARG_ROW_ID, rowId);
        args.putString(MessageListFragment.ARG_HIGHLIGHT, pattern);
        args.putBoolean(MessageListFragment.ARG_SHOW_IMMEDIATE, !animate);

        MessageListFragment fragment = MessageListFragment.getInstance(args);

        // Save the thread ID here and switch the content
        MainActivity.threadId = threadId;
        switchContent(fragment, animate);
    }

    @Override
    public void onOpen() {
        invalidateOptionsMenu();
        isContentHidden = true;

        // Notify the content that it is being closed, since the menu (i.e. conversation list) is
        // being opened.
        QKContentFragment.notifyOnContentClosing(content);

        // Hide the soft keyboard
        KeyboardUtils.hide(this, getCurrentFocus());

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onActionClicked(Snackbar snackbar) {
    }

    @Override
    public void onClose() {
        invalidateOptionsMenu();
        isContentHidden = false;

        // Notify the content that it is being opened, since the menu (i.e. conversation list) is
        // being closed.
        QKContentFragment.notifyOnContentOpening(content);

        // Hide the soft keyboard
        KeyboardUtils.hide(this, getCurrentFocus());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onOpened() {
        // When the menu (i.e. the conversation list) has been opened, the content has been opened.
        // So notify the content fragment.
        QKContentFragment.notifyOnContentClosed(content);
    }

    @Override
    public void onClosed() {
        // When the menu (i.e. the conversation list) has been closed, the content has been opened.
        // So notify the content fragment.
        QKContentFragment.notifyOnContentOpened(content);
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
    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener, Collection<Long> threadIds, boolean hasLockedMessages, Context context) {
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

        final QKCheckBox checkbox = (QKCheckBox) contents.findViewById(R.id.delete_locked);
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

    public static SharedPreferences getPrefs(Context context) {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return prefs;
    }

    public static Resources getRes(Context context) {
        if (res == null) {
            res = context.getResources();
        }
        return res;
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
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
                        @Override
                        public void run() {
                            int token = DELETE_CONVERSATION_TOKEN;
                            if (mThreadIds == null) {
                                Conversation.startDeleteAll(mHandler, token, mDeleteLockedMessages);
                                DraftCache.getInstance().refresh();
                            } else {
                                Conversation.startDelete(mHandler, token, mDeleteLockedMessages, mThreadIds);
                            }
                        }
                    }
            );
            dialog.dismiss();
        }
    }
}
