package com.moez.QKSMS.ui.messagelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.drm.DrmStore;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduPart;
import com.moez.QKSMS.LogTag;
import com.moez.QKSMS.MmsConfig;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.DialogHelper;
import com.moez.QKSMS.common.conversationdetails.ConversationDetailsDialog;
import com.moez.QKSMS.common.utils.DrmUtils;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.common.vcard.ContactOperations;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.ContactList;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.data.ConversationQueryHandler;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.interfaces.ActivityLauncher;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.base.QKContentFragment;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.delivery.DeliveryReportHelper;
import com.moez.QKSMS.ui.delivery.DeliveryReportItem;
import com.moez.QKSMS.ui.dialog.AsyncDialog;
import com.moez.QKSMS.ui.dialog.ConversationNotificationSettingsDialog;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.popup.QKComposeActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.ComposeView;
import com.moez.QKSMS.ui.view.MessageListRecyclerView;
import com.moez.QKSMS.ui.view.SmoothLinearLayoutManager;
import com.moez.QKSMS.ui.widget.WidgetProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class MessageListFragment extends QKContentFragment implements ActivityLauncher, SensorEventListener,
        LoaderManager.LoaderCallbacks<Cursor>, RecyclerCursorAdapter.MultiSelectListener,
        RecyclerCursorAdapter.ItemClickListener<MessageItem> {

    private final String TAG = "MessageListFragment";

    private final static boolean LOCAL_LOGV = false;

    private final int REQUEST_CODE_IMAGE = 6639;

    private static final int MESSAGE_LIST_QUERY_TOKEN = 9527;
    private static final int MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN = 9528;
    private static final int DELETE_MESSAGE_TOKEN = 9700;

    // Menu ID
    private static final int MENU_ADD_SUBJECT = 0;
    private static final int MENU_DELETE_THREAD = 1;
    private static final int MENU_ADD_ATTACHMENT = 2;
    private static final int MENU_DISCARD = 3;
    private static final int MENU_SEND = 4;
    private static final int MENU_CALL_RECIPIENT = 5;
    private static final int MENU_CONVERSATION_LIST = 6;
    private static final int MENU_DEBUG_DUMP = 7;

    // Context menu ID
    private static final int MENU_VIEW_CONTACT = 12;
    private static final int MENU_ADD_TO_CONTACTS = 13;

    private static final int MENU_EDIT_MESSAGE = 14;
    private static final int MENU_VIEW_SLIDESHOW = 16;
    private static final int MENU_VIEW_MESSAGE_DETAILS = 17;
    private static final int MENU_DELETE_MESSAGE = 18;
    private static final int MENU_SEARCH = 19;
    private static final int MENU_DELIVERY_REPORT = 20;
    private static final int MENU_FORWARD_MESSAGE = 21;
    private static final int MENU_CALL_BACK = 22;
    private static final int MENU_SEND_EMAIL = 23;
    private static final int MENU_COPY_MESSAGE_TEXT = 24;
    private static final int MENU_COPY_TO_SDCARD = 25;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 27;
    private static final int MENU_LOCK_MESSAGE = 28;
    private static final int MENU_UNLOCK_MESSAGE = 29;
    private static final int MENU_SAVE_RINGTONE = 30;
    private static final int MENU_PREFERENCES = 31;
    private static final int MENU_GROUP_PARTICIPANTS = 32;

    // When the conversation has a lot of messages and a new message is sent, the list is scrolled
    // so the user sees the just sent message. If we have to scroll the list more than 20 items,
    // then a scroll shortcut is invoked to move the list near the end before scrolling.
    private static final int MAX_ITEMS_TO_INVOKE_SCROLL_SHORTCUT = 20;

    // Any change in height in the message list view greater than this threshold will not
    // cause a smooth scroll. Instead, we jump the list directly to the desired position.
    private static final int SMOOTH_SCROLL_THRESHOLD = 200;

    // Whether or not we are currently enabled for SMS. This field is updated in onStart to make
    // sure we notice if the user has changed the default SMS app.
    private boolean mIsSmsEnabled;

    private Cursor mCursor;
    private MessageListAdapter mAdapter;
    private SmoothLinearLayoutManager mLayoutManager;
    private MessageListRecyclerView mRecyclerView;
    private Conversation mConversation;
    private ConversationLegacy mConversationLegacy;

    private boolean mOpened;
    private Sensor mProxSensor;
    private SensorManager mSensorManager;
    private AsyncDialog mAsyncDialog;
    private ComposeView mComposeView;
    private SharedPreferences mPrefs;
    private ConversationDetailsDialog mConversationDetailsDialog;

    private int mSavedScrollPosition = -1;  // we save the ListView's scroll position in onPause(),
    // so we can remember it after re-entering the activity.
    // If the value >= 0, then we jump to that line. If the
    // value is maxint, then we jump to the end.

    private long mLastMessageId;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private LoadConversationTask mLoadConversationTask;

    public static final String ARG_THREAD_ID = "threadId";
    public static final String ARG_ROW_ID = "rowId";
    public static final String ARG_HIGHLIGHT = "highlight";
    public static final String ARG_SHOW_IMMEDIATE = "showImmediate";

    private long mThreadId;
    private long mRowId;
    private String mHighlight;
    private boolean mShowImmediate;

    public static MessageListFragment getInstance(Bundle args) {
        MessageListFragment fragment = new MessageListFragment();

        // Update the fragment with the new arguments.
        fragment.updateArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mThreadId = savedInstanceState.getLong(ARG_THREAD_ID, -1);
            mRowId = savedInstanceState.getLong(ARG_ROW_ID, -1);
            mHighlight = savedInstanceState.getString(ARG_HIGHLIGHT, null);
            mShowImmediate = savedInstanceState.getBoolean(ARG_SHOW_IMMEDIATE, false);
        }

        mPrefs = mContext.getPrefs();
        mIsSmsEnabled = MmsConfig.isSmsEnabled(mContext);
        mConversationDetailsDialog = new ConversationDetailsDialog(mContext, getFragmentManager());
        setHasOptionsMenu(true);

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mProxSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (mPrefs.getBoolean(SettingsFragment.PROXIMITY_CALLING, false)) {
            mSensorManager.registerListener(this, mProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        mBackgroundQueryHandler = new BackgroundQueryHandler(mContext.getContentResolver());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure to notify that this conversation has been opened. This will mark it as read, load new drafts, etc.
        onOpenConversation();
    }

    // This is called by BaseContentFragment when updateArguments is called.
    @Override
    public void onNewArguments() {
        loadFromArguments();
    }

    public void loadFromArguments() {
        // Save the fields from the arguments
        Bundle args = getArguments();
        mThreadId = args.getLong(ARG_THREAD_ID, -1);
        mRowId = args.getLong(ARG_ROW_ID, -1);
        mHighlight = args.getString(ARG_HIGHLIGHT, null);
        mShowImmediate = args.getBoolean(ARG_SHOW_IMMEDIATE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        mRecyclerView = (MessageListRecyclerView) view.findViewById(R.id.conversation);

        mAdapter = new MessageListAdapter(mContext);
        mAdapter.setItemClickListener(this);
        mAdapter.setMultiSelectListener(this);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int position;

                if (mRowId != -1 && mCursor != null) {
                    // Scroll to the position in the conversation for that message.
                    position = SmsHelper.getPositionForMessageId(mCursor, "sms", mRowId, mAdapter.getColumnsMap());

                    // Be sure to reset the row ID here---we only want to scroll to the message
                    // the first time the cursor is loaded after the row ID is set.
                    mRowId = -1;

                } else {
                    position = mAdapter.getItemCount() - 1;
                }

                if (position != -1) {
                    manager.smoothScrollToPosition(mRecyclerView, null, position);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new SmoothLinearLayoutManager(mContext);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mComposeView = (ComposeView) view.findViewById(R.id.compose_view);
        mComposeView.setActivityLauncher(this);
        mComposeView.setLabel("MessageList");

        mRecyclerView.setComposeView(mComposeView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // All the data about the conversation, such as the thread ID and the row ID to skip to, is
        // stored in the arguments. So, calling this method will set up all the fields and then
        // perform initialization such as set up the Conversation object, make a query in the
        // adapter, etc.
        loadFromArguments();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_THREAD_ID, mThreadId);
        outState.putLong(ARG_ROW_ID, mRowId);
        outState.putString(ARG_HIGHLIGHT, mHighlight);
        outState.putBoolean(ARG_SHOW_IMMEDIATE, mShowImmediate);
    }

    public long getThreadId() {
        return mThreadId;
    }

    /**
     * To be called when the user opens a conversation. Initializes the Conversation objects, sets
     * up the draft, and marks the conversation as read.
     * <p/>
     * Note: This will have no effect if the context has not been initialized yet.
     */
    private void onOpenConversation() {
        new LoadConversationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    private void setTitle() {
        if (mContext != null && mConversation != null) {
            mContext.setTitle(mConversation.getRecipients().formatNames(", "));
        }
    }

    @Override
    public void onItemClick(final MessageItem messageItem, View view) {
        if (mAdapter.isInMultiSelectMode()) {
            mAdapter.toggleSelection(messageItem.getMessageId(), messageItem);
        } else {
            if (view.getId() == R.id.image_view || view.getId() == R.id.play_slideshow_button) {
                switch (messageItem.mAttachmentType) {
                    case SmsHelper.IMAGE:
                    case SmsHelper.AUDIO:
                    case SmsHelper.SLIDESHOW:
                        MessageUtils.viewMmsMessageAttachment(getActivity(), messageItem.mMessageUri, messageItem.mSlideshow, getAsyncDialog());
                        break;
                    case SmsHelper.VIDEO:
                        new QKDialog()
                                .setContext(mContext)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.stagefright_warning)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.yes, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        MessageUtils.viewMmsMessageAttachment(getActivity(), messageItem.mMessageUri, messageItem.mSlideshow, getAsyncDialog());
                                    }
                                })
                                .show(getFragmentManager(), null);
                        break;
                }
            } else if (messageItem != null && messageItem.isOutgoingMessage() && messageItem.isFailedMessage()) {
                showMessageResendOptions(messageItem);
            } else if (messageItem != null && ContentType.TEXT_VCARD.equals(messageItem.mTextContentType)) {
                openVcard(messageItem);
            } else {
                showMessageDetails(messageItem);
            }
        }
    }

    @Override
    public void onItemLongClick(MessageItem messageItem, View view) {

        QKDialog dialog = new QKDialog();
        dialog.setContext(mContext);
        dialog.setTitle(R.string.message_options);

        MsgListMenuClickListener l = new MsgListMenuClickListener(messageItem);

        // It is unclear what would make most sense for copying an MMS message
        // to the clipboard, so we currently do SMS only.
        if (messageItem.isSms()) {
            // Message type is sms. Only allow "edit" if the message has a single recipient
            if (getRecipients().size() == 1 && (messageItem.mBoxId == Telephony.Sms.MESSAGE_TYPE_OUTBOX || messageItem.mBoxId == Telephony.Sms.MESSAGE_TYPE_FAILED)) {
                dialog.addMenuItem(R.string.menu_edit, MENU_EDIT_MESSAGE);

            }

            dialog.addMenuItem(R.string.copy_message_text, MENU_COPY_MESSAGE_TEXT);
        }

        addCallAndContactMenuItems(dialog, messageItem);

        // Forward is not available for undownloaded messages.
        if (messageItem.isDownloaded() && (messageItem.isSms() || isForwardable(messageItem.getMessageId())) && mIsSmsEnabled) {
            dialog.addMenuItem(R.string.menu_forward, MENU_FORWARD_MESSAGE);
        }

        if (messageItem.isMms()) {
            switch (messageItem.mBoxId) {
                case Telephony.Mms.MESSAGE_BOX_INBOX:
                    break;
                case Telephony.Mms.MESSAGE_BOX_OUTBOX:
                    // Since we currently break outgoing messages to multiple
                    // recipients into one message per recipient, only allow
                    // editing a message for single-recipient conversations.
                    if (getRecipients().size() == 1) {
                        dialog.addMenuItem(R.string.menu_edit, MENU_EDIT_MESSAGE);
                    }
                    break;
            }
            switch (messageItem.mAttachmentType) {
                case SmsHelper.TEXT:
                    break;
                case SmsHelper.VIDEO:
                case SmsHelper.IMAGE:
                    if (haveSomethingToCopyToSDCard(messageItem.mMsgId)) {
                        dialog.addMenuItem(R.string.copy_to_sdcard, MENU_COPY_TO_SDCARD);
                    }
                    break;
                case SmsHelper.SLIDESHOW:
                default:
                    dialog.addMenuItem(R.string.view_slideshow, MENU_VIEW_SLIDESHOW);
                    if (haveSomethingToCopyToSDCard(messageItem.mMsgId)) {
                        dialog.addMenuItem(R.string.copy_to_sdcard, MENU_COPY_TO_SDCARD);
                    }
                    if (isDrmRingtoneWithRights(messageItem.mMsgId)) {
                        dialog.addMenuItem(getDrmMimeMenuStringRsrc(messageItem.mMsgId), MENU_SAVE_RINGTONE);
                    }
                    break;
            }
        }

        if (messageItem.mLocked && mIsSmsEnabled) {
            dialog.addMenuItem(R.string.menu_unlock, MENU_UNLOCK_MESSAGE);
        } else if (mIsSmsEnabled) {
            dialog.addMenuItem(R.string.menu_lock, MENU_LOCK_MESSAGE);
        }

        dialog.addMenuItem(R.string.view_message_details, MENU_VIEW_MESSAGE_DETAILS);

        if (messageItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE || messageItem.mReadReport) {
            dialog.addMenuItem(R.string.view_delivery_report, MENU_DELIVERY_REPORT);
        }

        if (mIsSmsEnabled) {
            dialog.addMenuItem(R.string.delete_message, MENU_DELETE_MESSAGE);
        }

        dialog.buildMenu(l);
        dialog.show(getFragmentManager(), "messagelistitem options");
    }

    private void addCallAndContactMenuItems(QKDialog dialog, MessageItem msgItem) {
        if (TextUtils.isEmpty(msgItem.mBody)) {
            return;
        }
        SpannableString msg = new SpannableString(msgItem.mBody);
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris = MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));

        // Remove any dupes so they don't getConversation added to the menu multiple times
        HashSet<String> collapsedUris = new HashSet<>();
        for (String uri : uris) {
            collapsedUris.add(uri.toLowerCase());
        }
        for (String uriString : collapsedUris) {
            String prefix = null;
            int sep = uriString.indexOf(":");
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                uriString = uriString.substring(sep + 1);
            }
            Uri contactUri = null;
            boolean knownPrefix = true;
            if ("mailto".equalsIgnoreCase(prefix)) {
                contactUri = getContactUriForEmail(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                contactUri = getContactUriForPhoneNumber(uriString);
            } else {
                knownPrefix = false;
            }
            if (knownPrefix && contactUri == null) {
                Intent intent = MainActivity.createAddContactIntent(uriString);

                String addContactString = getString(R.string.menu_add_address_to_contacts, uriString);
                dialog.addMenuItem(addContactString, MENU_ADD_ADDRESS_TO_CONTACTS);
            }
        }
    }

    private Uri getContactUriForEmail(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[]{ContactsContract.CommonDataKinds.Email.CONTACT_ID, ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(1);
                    if (!TextUtils.isEmpty(name)) {
                        return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, cursor.getLong(0));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private Uri getContactUriForPhoneNumber(String phoneNumber) {
        Contact contact = Contact.get(phoneNumber, false);
        if (contact.existsInDatabase()) {
            return contact.getUri();
        }
        return null;
    }

    /**
     * Returns true if all drm'd parts are forwardable.
     *
     * @param msgId
     * @return true if all drm'd parts are forwardable.
     */
    private boolean isForwardable(long msgId) {
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(mContext, ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "getDrmMimeType can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (DrmUtils.isDrmType(type) && !DrmUtils.haveRightsForAction(part.getDataUri(),
                    DrmStore.Action.TRANSFER)) {
                return false;
            }
        }
        return true;
    }

    private ContactList getRecipients() {
        // If the recipients editor is visible, the conversation has
        // not really officially 'started' yet.  Recipients will be set
        // on the conversation once it has been saved or sent.  In the
        // meantime, let anyone who needs the recipient list think it
        // is empty rather than giving them a stale one.
        /*if (isRecipientsEditorVisible()) {
            if (sEmptyContactList == null) {
                sEmptyContactList = new ContactList();
            }
            return sEmptyContactList;
        }*/
        return mConversation.getRecipients();
    }

    /**
     * Looks to see if there are any valid parts of the attachment that can be copied to a SD card.
     *
     * @param msgId
     */
    private boolean haveSomethingToCopyToSDCard(long msgId) {
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(mContext, ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "haveSomethingToCopyToSDCard can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        boolean result = false;
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                Log.v(TAG, "[CMA] haveSomethingToCopyToSDCard: part[" + i + "] contentType=" + type);
            }

            if (ContentType.isImageType(type) || ContentType.isVideoType(type) ||
                    ContentType.isAudioType(type) || DrmUtils.isDrmType(type)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private int getDrmMimeMenuStringRsrc(long msgId) {
        if (isDrmRingtoneWithRights(msgId)) {
            return R.string.save_ringtone;
        }
        return 0;
    }

    AsyncDialog getAsyncDialog() {
        if (mAsyncDialog == null) {
            mAsyncDialog = new AsyncDialog(getActivity());
        }
        return mAsyncDialog;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_call:
                makeCall();
                return true;

            case R.id.menu_notifications:
                ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(mContext, mThreadId);
                conversationPrefs.putBoolean(SettingsFragment.NOTIFICATIONS, !conversationPrefs.getNotificationsEnabled());
                mContext.invalidateOptionsMenu();
                return true;

            case R.id.menu_details:
                mConversationDetailsDialog.showDetails(mConversation);
                return true;

            case R.id.menu_notification_settings:
                ConversationNotificationSettingsDialog.newInstance(mThreadId, mConversation.getRecipients().formatNames(", "))
                        .setContext(mContext)
                        .show(getFragmentManager(), "notification prefs");
                return true;

            case R.id.menu_delete_conversation:
                DialogHelper.showDeleteConversationDialog((MainActivity) mContext, mThreadId);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeCall() {
        Intent openDialerIntent = new Intent(Intent.ACTION_CALL);
        openDialerIntent.setData(Uri.parse("tel:" + mConversationLegacy.getAddress()));
        startActivity(openDialerIntent);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save the draft. This should also clear the EditText.
        mComposeView.saveDraft();
    }

    /**
     * Photo Selection result
     */
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (!mComposeView.onActivityResult(requestCode, resultCode, data)) {
            // Wasn't handled by ComposeView
        }
    }

    /**
     * Should only be called for failed messages. Deletes the message, placing the text from the
     * message back in the edit box to be updated and then sent.
     * <p/>
     * Assumes that cursor points to the correct MessageItem.
     *
     * @param msgItem
     */
    private void editMessageItem(MessageItem msgItem) {
        String body = msgItem.mBody;

        // Delete the message and put the text back into the edit text.
        deleteMessageItem(msgItem);

        // Set the text and open the keyboard
        KeyboardUtils.show(mContext);

        mComposeView.setText(body);
    }

    /**
     * Should only be called for failed messages. Deletes the message and resends it.
     *
     * @param msgItem
     */
    public void resendMessageItem(final MessageItem msgItem) {
        String body = msgItem.mBody;
        deleteMessageItem(msgItem);

        mComposeView.setText(body);
        mComposeView.sendSms();
    }

    /**
     * Deletes the message from the conversation list and the conversation history.
     *
     * @param msgItem
     */
    public void deleteMessageItem(final MessageItem msgItem) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... none) {
                if (msgItem.isMms()) {
                    MessageUtils.removeThumbnailsFromCache(msgItem.getSlideshow());

                    QKSMSApp.getApplication().getPduLoaderManager().removePdu(msgItem.mMessageUri);
                    // Delete the message *after* we've removed the thumbnails because we
                    // need the pdu and slideshow for removeThumbnailsFromCache to work.
                }

                // Determine if we're deleting the last item in the cursor.
                Boolean deletingLastItem = false;
                if (mAdapter != null && mAdapter.getCursor() != null) {
                    mCursor = mAdapter.getCursor();
                    mCursor.moveToLast();
                    long msgId = mCursor.getLong(MessageColumns.COLUMN_ID);
                    deletingLastItem = msgId == msgItem.mMsgId;
                }

                mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN, deletingLastItem,
                        msgItem.mMessageUri, msgItem.mLocked ? null : "locked=0", null);
                return null;
            }
        }.execute();
    }

    private void initLoaderManager() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onContentOpening() {
        super.onContentOpening();
        mOpened = false; // We're animating the fragment in, this flag warns us not to do anything heavy
    }

    @Override
    public void onContentOpened() {
        super.onContentOpened();
        mOpened = true; // The fragment has finished animating in

        if (mPrefs != null && mPrefs.getBoolean(SettingsFragment.PROXIMITY_CALLING, false)) {
            mSensorManager.registerListener(this, mProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onContentClosing() {
    }

    @Override
    public void onContentClosed() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        if (mOpened) {
            if (mConversationLegacy != null) {
                mConversationLegacy.markRead();
            }

            if (mConversation != null) {
                mConversation.blockMarkAsRead(true);
                mConversation.markAsRead();
                mComposeView.saveDraft();
            }
        }
    }

    @Override
    public void inflateToolbar(Menu menu, MenuInflater inflater, Context context) {
        inflater.inflate(R.menu.conversation, menu);
        setTitle();

        ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(context, mThreadId);
        menu.findItem(R.id.menu_notifications).setTitle(conversationPrefs.getNotificationsEnabled() ?
                R.string.menu_notifications : R.string.menu_notifications_off);
        menu.findItem(R.id.menu_notifications).setIcon(conversationPrefs.getNotificationsEnabled() ?
                R.drawable.ic_notifications : R.drawable.ic_notifications_off);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0) {
            makeCall();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignored
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext,
                Uri.withAppendedPath(Message.MMS_SMS_CONTENT_PROVIDER, String.valueOf(mThreadId)),
                MessageColumns.PROJECTION, null, null, "normalized_date ASC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            // Swap the new cursor in.  (The framework will take care of closing the, old cursor once we return.)
            mAdapter.changeCursor(data);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    public void onMultiSelectStateChanged(boolean enabled) {

    }

    @Override
    public void onItemAdded(long id) {

    }

    @Override
    public void onItemRemoved(long id) {

    }

    private class DeleteMessageListener implements DialogInterface.OnClickListener {
        private final MessageItem mMessageItem;

        public DeleteMessageListener(MessageItem messageItem) {
            mMessageItem = messageItem;
        }

        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            deleteMessageItem(mMessageItem);
        }
    }

    /**
     * Context menu handlers for the message list view.
     */
    private final class MsgListMenuClickListener implements AdapterView.OnItemClickListener {
        private MessageItem mMsgItem;

        public MsgListMenuClickListener(MessageItem msgItem) {
            mMsgItem = msgItem;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mMsgItem == null) {
                return;
            }

            switch ((int) id) {
                case MENU_EDIT_MESSAGE:
                    editMessageItem(mMsgItem);
                    break;

                case MENU_COPY_MESSAGE_TEXT:
                    copyToClipboard(mMsgItem.mBody);
                    break;

                case MENU_FORWARD_MESSAGE:
                    forwardMessage(mMsgItem);
                    break;

                case MENU_VIEW_SLIDESHOW:
                    MessageUtils.viewMmsMessageAttachment(getActivity(), ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, mMsgItem.mMsgId), null, getAsyncDialog());
                    break;

                case MENU_VIEW_MESSAGE_DETAILS:
                    showMessageDetails(mMsgItem);
                    break;

                case MENU_DELETE_MESSAGE:
                    DeleteMessageListener l = new DeleteMessageListener(mMsgItem);
                    confirmDeleteDialog(l, mMsgItem.mLocked);
                    break;

                case MENU_DELIVERY_REPORT:
                    showDeliveryReport(mMsgItem.mMsgId, mMsgItem.mType);
                    break;

                case MENU_COPY_TO_SDCARD: {
                    int resId = copyMedia(mMsgItem.mMsgId) ? R.string.copy_to_sdcard_success : R.string.copy_to_sdcard_fail;
                    Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
                    break;
                }

                case MENU_SAVE_RINGTONE: {
                    int resId = getDrmMimeSavedStringRsrc(mMsgItem.mMsgId, saveRingtone(mMsgItem.mMsgId));
                    Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
                    break;
                }

                case MENU_ADD_ADDRESS_TO_CONTACTS:
                    addToContacts(mMsgItem);
                    break;

                case MENU_LOCK_MESSAGE:
                    lockMessage(mMsgItem, true);
                    break;

                case MENU_UNLOCK_MESSAGE:
                    lockMessage(mMsgItem, false);
                    break;
            }
        }
    }

    private void addToContacts(MessageItem msgItem) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, msgItem.mAddress);
        mContext.startActivity(intent);
    }

    private void copyToClipboard(String str) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(null, str));
    }

    private void forwardMessage(MessageItem msgItem) {
        Intent forwardIntent = new Intent(mContext, QKComposeActivity.class);
        forwardIntent.putExtra("sms_body", msgItem.mBody);
        startActivity(forwardIntent);
    }

    private void lockMessage(MessageItem msgItem, boolean locked) {
        Uri uri;
        if ("sms".equals(msgItem.mType)) {
            uri = Telephony.Sms.CONTENT_URI;
        } else {
            uri = Telephony.Mms.CONTENT_URI;
        }
        final Uri lockUri = ContentUris.withAppendedId(uri, msgItem.mMsgId);

        final ContentValues values = new ContentValues(1);
        values.put("locked", locked ? 1 : 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mContext.getContentResolver().update(lockUri,
                        values, null, null);
            }
        }, "MainActivity.lockMessage").start();
    }

    private boolean showMessageResendOptions(final MessageItem msgItem) {
        final Cursor cursor = mAdapter.getCursorForItem(msgItem);
        if (cursor == null) {
            return false;
        }

        KeyboardUtils.hide(mContext, mComposeView);

        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.failed_message_title)
                .setItems(R.array.resend_menu, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0: // Resend message
                                resendMessageItem(msgItem);

                                break;
                            case 1: // Edit message
                                editMessageItem(msgItem);

                                break;
                            case 2: // Delete message
                                confirmDeleteDialog(new DeleteMessageListener(msgItem), false);
                                break;
                        }
                    }
                }).show(getFragmentManager(), QKDialog.LIST_TAG);
        return true;
    }

    private void openVcard(MessageItem messageItem) {
        Log.d(TAG, "Vcard: " + messageItem.mBody);

        VCard vCard = Ezvcard.parse(messageItem.mBody).first();

        ContactOperations operations = new ContactOperations(mContext);
        try {
            operations.insertContact(vCard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean showMessageDetails(MessageItem msgItem) {
        Cursor cursor = mAdapter.getCursorForItem(msgItem);
        if (cursor == null) {
            return false;
        }
        String messageDetails = MessageUtils.getMessageDetails(mContext, cursor, msgItem.mMessageSize);
        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.message_details_title)
                .setMessage(messageDetails)
                .setCancelOnTouchOutside(true)
                .show(getFragmentManager(), QKDialog.DETAILS_TAG);
        return true;
    }

    private void confirmDeleteDialog(DialogInterface.OnClickListener listener, boolean locked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setMessage(locked ? R.string.confirm_delete_locked_message : R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showDeliveryReport(long messageId, String type) {
        DeliveryReportHelper deliveryReportHelper = new DeliveryReportHelper(mContext, messageId, type);
        List<DeliveryReportItem> deliveryReportItems = deliveryReportHelper.getListItems();

        String[] items = new String[deliveryReportItems.size() * 3];
        for (int i = 0; i < deliveryReportItems.size() * 3; i++) {
            switch (i % 3) {
                case 0:
                    items[i] = deliveryReportItems.get(i - (i / 3)).recipient;
                    break;
                case 1:
                    items[i] = deliveryReportItems.get(i - 1 - ((i - 1) / 3)).status;
                    break;
                case 2:
                    items[i] = deliveryReportItems.get(i - 2 - ((i - 2) / 3)).deliveryDate;
                    break;
            }
        }

        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.delivery_header_title)
                .setItems(items, null)
                .setPositiveButton(R.string.okay, null)
                .show(getFragmentManager(), "delivery report");
    }

    /**
     * Copies media from an Mms to the "download" directory on the SD card. If any of the parts
     * are audio types, drm'd or not, they're copied to the "Ringtones" directory.
     *
     * @param msgId
     */
    private boolean copyMedia(long msgId) {
        boolean result = true;
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(mContext, ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "copyMedia can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);

            // all parts have to be successful for a valid result.
            result &= copyPart(part, Long.toHexString(msgId));
        }
        return result;
    }

    private boolean copyPart(PduPart part, String fallback) {
        Uri uri = part.getDataUri();
        String type = new String(part.getContentType());
        boolean isDrm = DrmUtils.isDrmType(type);
        if (isDrm) {
            type = QKSMSApp.getApplication().getDrmManagerClient().getOriginalMimeType(part.getDataUri());
        }
        if (!ContentType.isImageType(type) && !ContentType.isVideoType(type) &&
                !ContentType.isAudioType(type)) {
            return true;    // we only save pictures, videos, and sounds. Skip the text parts,
            // the app (smil) parts, and other type that we can't handle.
            // Return true to pretend that we successfully saved the part so
            // the whole save process will be counted a success.
        }
        InputStream input = null;
        FileOutputStream fout = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }

                String fileName;
                if (location == null) {
                    // Use fallback name.
                    fileName = fallback;
                } else {
                    // For locally captured videos, fileName can end up being something like this:
                    //      /mnt/sdcard/Android/data/com.android.mms/cache/.temp1.3gp
                    fileName = new String(location);
                }
                File originalFile = new File(fileName);
                fileName = originalFile.getName();  // Strip the full path of where the "part" is
                // stored down to just the leaf filename.

                // Depending on the location, there may be an
                // extension already on the name or not. If we've got audio, put the attachment
                // in the Ringtones directory.
                String dir = Environment.getExternalStorageDirectory() + "/"
                        + (ContentType.isAudioType(type) ? Environment.DIRECTORY_RINGTONES :
                        Environment.DIRECTORY_DOWNLOADS) + "/";
                String extension;
                int index;
                if ((index = fileName.lastIndexOf('.')) == -1) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                }
                if (isDrm) {
                    extension += DrmUtils.getConvertExtension(type);
                }
                // Remove leading periods. The gallery ignores files starting with a period.
                fileName = fileName.replaceAll("^.", "");

                File file = getUniqueDestination(dir + fileName, extension);

                // make sure the path is valid and directories created for this file.
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                    return false;
                }

                fout = new FileOutputStream(file);

                byte[] buffer = new byte[8000];
                int size = 0;
                while ((size = fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, size);
                }

                // Notify other applications listening to scanner events
                // that a media file has been added to the sd card
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }

    private File getUniqueDestination(String base, String extension) {
        File file = new File(base + "." + extension);

        for (int i = 2; file.exists(); i++) {
            file = new File(base + "_" + i + "." + extension);
        }
        return file;
    }

    private int getDrmMimeSavedStringRsrc(long msgId, boolean success) {
        if (isDrmRingtoneWithRights(msgId)) {
            return success ? R.string.saved_ringtone : R.string.saved_ringtone_fail;
        }
        return 0;
    }

    /**
     * Returns true if any part is drm'd audio with ringtone rights.
     *
     * @param msgId
     * @return true if one of the parts is drm'd audio with rights to save as a ringtone.
     */
    private boolean isDrmRingtoneWithRights(long msgId) {
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(mContext, ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "isDrmRingtoneWithRights can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (DrmUtils.isDrmType(type)) {
                String mimeType = QKSMSApp.getApplication().getDrmManagerClient()
                        .getOriginalMimeType(part.getDataUri());
                if (ContentType.isAudioType(mimeType) && DrmUtils.haveRightsForAction(part.getDataUri(),
                        DrmStore.Action.RINGTONE)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Copies media from an Mms to the DrmProvider
     *
     * @param msgId
     */
    private boolean saveRingtone(long msgId) {
        boolean result = true;
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(mContext, ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            Log.e(TAG, "copyToDrmProvider can't load pdu body: " + msgId);
        }
        if (body == null) {
            return false;
        }

        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());

            if (DrmUtils.isDrmType(type)) {
                // All parts (but there's probably only a single one) have to be successful
                // for a valid result.
                result &= copyPart(part, Long.toHexString(msgId));
            }
        }
        return result;
    }

    private void startMsgListQuery(int token) {
        /*if (mSendDiscreetMode) {
            return;
        }*/
        Uri conversationUri = mConversation.getUri();

        if (conversationUri == null) {
            Log.v(TAG, "##### startMsgListQuery: conversationUri is null, bail!");
            return;
        }

        long threadId = mConversation.getThreadId();
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "startMsgListQuery for " + conversationUri + ", threadId=" + threadId +
                    " token: " + token + " mConversation: " + mConversation);
        }

        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(token);
        try {
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
                    token,
                    threadId /* cookie */,
                    conversationUri,
                    MessageColumns.PROJECTION,
                    null, null, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(mContext, e);
        }
    }

    private final class BackgroundQueryHandler extends ConversationQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case MainActivity.HAVE_LOCKED_MESSAGES_TOKEN:
                    if (mContext.isFinishing()) {
                        Log.w(TAG, "ComposeMessageActivity is finished, do nothing ");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    ArrayList<Long> threadIds = (ArrayList<Long>) cookie;
                    MainActivity.confirmDeleteThreadDialog(
                            new MainActivity.DeleteThreadListener(threadIds, mBackgroundQueryHandler, mContext), threadIds,
                            cursor != null && cursor.getCount() > 0, mContext);
                    if (cursor != null) {
                        cursor.close();
                    }
                    break;

                case MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN:
                    // check consistency between the query result and 'mConversation'
                    long tid = (Long) cookie;

                    if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        Log.v(TAG, "##### onQueryComplete (after delete): msg history result for threadId " + tid);
                    }
                    if (cursor == null) {
                        return;
                    }
                    if (tid > 0 && cursor.getCount() == 0) {
                        // We just deleted the last message and the thread will getConversation deleted
                        // by a trigger in the database. Clear the threadId so next time we
                        // need the threadId a new thread will getConversation created.
                        Log.v(TAG, "##### MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN clearing thread id: " + tid);
                        Conversation conv = Conversation.getConversation(mContext, tid, false);
                        if (conv != null) {
                            conv.clearThreadId();
                            conv.setDraftState(false);
                        }
                        // The last message in this converation was just deleted. Send the user
                        // to the conversation list.
                        ((MainActivity) mContext).showMenu();
                    }
                    cursor.close();
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            switch (token) {
                case MainActivity.DELETE_CONVERSATION_TOKEN:
                    mConversation.setMessageCount(0);
                    // fall through
                case DELETE_MESSAGE_TOKEN:
                    if (cookie instanceof Boolean && ((Boolean) cookie).booleanValue()) {
                        // If we just deleted the last message, reset the saved id.
                        mLastMessageId = 0;
                    }

                    // Update the notification for new messages since they may be deleted.
                    NotificationManager.update(mContext);

                    // TODO Update the notification for failed messages since they may be deleted.
                    //updateSendFailedNotification();
                    break;
            }
            // If we're deleting the whole conversation, throw away our current working message and bail.
            if (token == MainActivity.DELETE_CONVERSATION_TOKEN) {
                ContactList recipients = mConversation.getRecipients();

                // Remove any recipients referenced by this single thread from the It's possible for two or more
                // threads to reference the same contact. That's ok if we remove it. We'll recreate that contact
                // when we init all Conversations below.
                if (recipients != null) {
                    for (Contact contact : recipients) {
                        contact.removeFromCache();
                    }
                }

                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(mContext);

                // Go back to the conversation list
                ((MainActivity) mContext).showMenu();
            } else if (token == DELETE_MESSAGE_TOKEN) {
                // Check to see if we just deleted the last message
                startMsgListQuery(MESSAGE_LIST_QUERY_AFTER_DELETE_TOKEN);
            }

            WidgetProvider.notifyDatasetChanged(mContext);
        }
    }

    private class LoadConversationTask extends AsyncTask<Void, Void, Void> {

        public LoadConversationTask() {
            Log.d(TAG, "LoadConversationTask");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Loading conversation");
            mConversation = Conversation.getConversation(mContext, mThreadId, true);
            mConversationLegacy = new ConversationLegacy(mContext, mThreadId);

            mConversationLegacy.markRead();
            mConversation.blockMarkAsRead(true);
            mConversation.markAsRead();

            // Delay the thread until the fragment has finished opening. If it waits longer than
            // 10 seconds, then something is wrong, so cancel it. This happens when the fragment is closed before
            // it opens, or the screen is rotated, and then "mOpened" never gets changed to true,
            // leaving this thread running forever. This issue is actually what caused the great
            // QKSMS battery drain of 2015
            long time = System.currentTimeMillis();
            while (!mOpened) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - time > 10000) {
                    Log.w(TAG, "Task running for over 10 seconds, something is wrong");
                    cancel(true);
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "Conversation loaded");

            mComposeView.onOpenConversation(mConversation, mConversationLegacy);
            setTitle();

            mAdapter.setIsGroupConversation(mConversation.getRecipients().size() > 1);

            if (isAdded()) {
                initLoaderManager();
            }
        }
    }
}
