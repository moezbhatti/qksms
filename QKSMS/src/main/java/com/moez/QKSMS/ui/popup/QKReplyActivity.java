package com.moez.QKSMS.ui.popup;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.interfaces.ActivityLauncher;
import com.moez.QKSMS.service.CopyUnreadMessageTextService;
import com.moez.QKSMS.service.DeleteUnreadMessageService;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.base.QKPopupActivity;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.view.ComposeView;

public class QKReplyActivity extends QKPopupActivity implements DialogInterface.OnDismissListener,
        LoaderManager.LoaderCallbacks<Cursor>, ActivityLauncher, ComposeView.OnSendListener {

    @SuppressWarnings("unused")
    private String TAG = "QKReplyActivity";

    // Intent extras for configuring a QuickReplyActivity intent
    public static final String EXTRA_THREAD_ID = "thread_id";
    public static final String EXTRA_SHOW_KEYBOARD = "open_keyboard";

    public static boolean sIsShowing = false;
    private static long sThreadId;

    private Conversation mConversation;
    private ConversationLegacy mConversationLegacy;

    private Cursor mCursor;
    private boolean mShowUnreadOnly = true;

    private ListView mListView;
    private QKReplyAdapter mAdapter;
    private ComposeView mComposeView;

    /**
     * True if we're starting an activity. This will let us know whether or not we should finish()
     * in onPause().
     */
    private boolean mIsStartingActivity = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        sThreadId = extras.getLong(EXTRA_THREAD_ID);
        mConversation = Conversation.getConversation(this, sThreadId, false);
        mConversationLegacy = new ConversationLegacy(this, sThreadId);

        // Set up the compose view.
        mComposeView = (ComposeView) findViewById(R.id.compose_view);
        mComposeView.setActivityLauncher(this);
        mComposeView.setOnSendListener(this);
        mComposeView.setLabel("QKReply");
        mComposeView.refresh();

        mAdapter = new QKReplyAdapter(this);

        mListView = (ListView) findViewById(R.id.popup_messages);
        mListView.setAdapter(mAdapter);

        // Set the conversation data objects. These are used to save drafts, send sms messages, etc.
        mComposeView.onOpenConversation(mConversation, mConversationLegacy);

        // If the user has the "show keyboard" for popups option enabled, show the keyboard here by
        // requesting the focus on the reply text.
        if (extras.getBoolean(EXTRA_SHOW_KEYBOARD, false)) {
            mComposeView.requestReplyTextFocus();
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                mConversationLegacy.getName(true);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                setTitle(mConversationLegacy.getName(true));

                initLoaderManager();
            }
        }.execute((Void[]) null);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_qkreply;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qkreply, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void switchView() {
        mShowUnreadOnly = !mShowUnreadOnly;
        getLoaderManager().restartLoader(0, null, this);
    }

    private void initLoaderManager() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        KeyboardUtils.hide(this, mComposeView);

        mComposeView.saveDraft();

        sIsShowing = false;

        // When the home button is pressed, this ensures that the QK Reply is shut down
        // Don't shut it down if it pauses and the screen is off though
        if (!mIsStartingActivity && !isChangingConfigurations() && isScreenOn()) {
            finish();
        }
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
    protected void onResume() {
        super.onResume();
        sIsShowing = true;
        sThreadId = mConversationLegacy.getThreadId();
        mIsStartingActivity = false;
    }

    @Override
    public void finish() {
        // Override pending transitions so that we don't see black for a second when QuickReply
        // closes
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_fold:
                switchView();
                if (mShowUnreadOnly) {
                    item.setIcon(R.drawable.ic_unfold);
                    item.setTitle(R.string.menu_show_all);
                } else {
                    item.setIcon(R.drawable.ic_fold);
                    item.setTitle(R.string.menu_show_unread);
                }
                return true;

            case R.id.menu_open_thread:
                Intent threadIntent = new Intent(this, MainActivity.class);
                threadIntent.putExtra(MainActivity.EXTRA_THREAD_ID, mConversationLegacy.getThreadId());
                startActivity(threadIntent);
                finish();
                return true;

            case R.id.menu_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + mConversationLegacy.getAddress()));
                startActivity(callIntent);
                return true;

            case R.id.menu_mark_read:
                mConversationLegacy.markRead();
                finish();
                return true;

            case R.id.menu_delete:
                Intent intent = new Intent(this, DeleteUnreadMessageService.class);
                intent.putExtra(DeleteUnreadMessageService.EXTRA_THREAD_URI, mConversation.getUri());
                startService(intent);
                finish();
                return true;

            case R.id.menu_copy:
                Intent copyIntent = new Intent(this, CopyUnreadMessageTextService.class);
                copyIntent.putExtra(DeleteUnreadMessageService.EXTRA_THREAD_URI, mConversation.getUri());
                startService(copyIntent);
                return true;

            case R.id.menu_forward:
                Intent forwardIntent = new Intent(this, QKComposeActivity.class);
                forwardIntent.putExtra("sms_body", SmsHelper.getUnreadMessageText(this, mConversation.getUri()));
                startActivity(forwardIntent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = mShowUnreadOnly ? SmsHelper.UNREAD_SELECTION : null;
        return new CursorLoader(this,
                Uri.withAppendedPath(Message.MMS_SMS_CONTENT_PROVIDER, "" + mConversationLegacy.getThreadId()),
                MessageColumns.PROJECTION, selection, null, "normalized_date ASC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            mAdapter.changeCursor(data);
        }
        mCursor = data;
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
        mCursor = null;
    }

    /**
     * Other areas of the app can tell the QK Reply window to close itself if necessary
     * <p/>
     * 1. MainActivity. When it's resumed, we don't want any QK Reply windows to open, which may have
     * happened while the screen was off.
     * <p/>
     * 2. PushbulletService. If a message is replied to via PB, close the window
     * <p/>
     * 3. MarkReadReceiver. A QK Reply window may have opened while the screen was off, so if it's marked
     * as read from the lock screen via notification, the QK Reply window should be dismissed
     */
    public static void dismiss(long threadId) {
        if (sThreadId == threadId) {
            sIsShowing = false;
            sThreadId = 0;
            System.exit(0);
        }
    }

    @Override
    public void onSend(String[] addresses, String body) {
        // When we send an SMS, mark the conversation as read, and close the quick reply activity.
        if (mConversation != null) {
            mConversation.markAsRead();
        }
        if (mConversationLegacy != null) {
            mConversationLegacy.markRead();
        }

        finish();
    }

    /**
     * Launches an activity with the given request code.
     * <p/>
     * Note: The `ActivityLauncher` interface exposes this method for views (such as ComposeView)
     * to use.
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        mIsStartingActivity = true;

        // 0 for the exit anim so that the dialog appears to not fade out while you're choosing an
        // attachment.
        overridePendingTransition(R.anim.abc_fade_in, 0);
    }
}
