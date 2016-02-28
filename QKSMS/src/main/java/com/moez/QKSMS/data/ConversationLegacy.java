package com.moez.QKSMS.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;
import com.moez.QKSMS.R;
import com.moez.QKSMS.receiver.UnreadBadgeService;
import com.moez.QKSMS.common.google.DraftCache;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.transaction.SqliteWrapper;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.messagelist.MessageItem;

/**
 * Use this class (rather than Conversation) for marking conversations as read, and managing drafts.
 */
public class ConversationLegacy {
    private final String TAG = "ConversationLegacy";

    public static final Uri CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true");
    public static final Uri ADDRESSES_CONTENT_PROVIDER = Uri.parse("content://mms-sms/canonical-addresses");

    public static final int COLUMN_ADDRESSES_ADDRESS = 1;

    private ContactHelper contactHelper;
    private Context context;

    private long threadId;
    private String name;
    private String address;
    private long recipient;
    private String draft;
    private int type;

    private Cursor cursor;

    public ConversationLegacy(Context context, long threadId) {
        this.context = context;
        this.threadId = threadId;
        contactHelper = new ContactHelper();
    }

    public long getThreadId() {
        return threadId;
    }

    public Uri getUri() {
        return Uri.parse("content://mms-sms/conversations/" + getThreadId());
    }


    public String getName(boolean findIfNull) {
        if (name == null || name.trim().isEmpty()) {
            if (findIfNull) name = contactHelper.getName(context, getAddress());
            else return getAddress();
        }

        return name;
    }

    public String getAddress() {
        if (address == null) {
            if (getType() == 0) { //Single person
                try {
                    cursor = context.getContentResolver().query(ADDRESSES_CONTENT_PROVIDER, null, "_id=" + getRecipient(), null, null);
                    cursor.moveToFirst();
                    address = cursor.getString(COLUMN_ADDRESSES_ADDRESS);

                    address = PhoneNumberUtils.stripSeparators(address);

                    if (address == null || address.isEmpty()) {
                        cursor = context.getContentResolver().query(SmsHelper.RECEIVED_MESSAGE_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_ID}, "thread_id=" + threadId, null, SmsHelper.sortDateDesc);
                        cursor.moveToFirst();

                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));
                        address = new Message(context, id).getAddress();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }

        return address;
    }

    public long getRecipient() {
        if (recipient == 0) {
            try {
                cursor = context.getContentResolver().query(CONVERSATIONS_CONTENT_PROVIDER, null, "_id=" + threadId, null, null);
                cursor.moveToFirst();
                recipient = cursor.getInt(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_RECIPIENT));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return recipient;
    }

    public boolean hasDraft() {
        return DraftCache.getInstance().hasDraft(threadId);
    }

    public String getDraft() {

        if (draft == null) {
            try {
                cursor = context.getContentResolver().query(SmsHelper.DRAFTS_CONTENT_PROVIDER, null, SmsHelper.COLUMN_THREAD_ID + "=" + threadId, null, null);
                cursor.moveToFirst();
                draft = cursor.getString(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_BODY));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return draft;
    }

    public void clearDrafts() {
        if (hasDraft()) {
            try {
                DraftCache.getInstance().setSavingDraft(true);
                DraftCache.getInstance().setDraftState(threadId, false);

                cursor = context.getContentResolver().query(SmsHelper.DRAFTS_CONTENT_PROVIDER, null, SmsHelper.COLUMN_THREAD_ID + "=" + threadId, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        context.getContentResolver().delete(Uri.parse("content://sms/" + cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ID))), null, null);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                DraftCache.getInstance().setSavingDraft(false);
            }
        }
    }

    public void saveDraft(final String draft) {

        clearDrafts();

        if (draft.length() > 0) {
            try {
                DraftCache.getInstance().setSavingDraft(true);
                DraftCache.getInstance().setDraftState(threadId, true);
                ConversationLegacy.this.draft = draft;

                ContentResolver contentResolver = context.getContentResolver();
                ContentValues cv = new ContentValues();

                cv.put("address", getAddress());
                cv.put("body", draft);

                contentResolver.insert(SmsHelper.DRAFTS_CONTENT_PROVIDER, cv);
            } finally {
                DraftCache.getInstance().setSavingDraft(false);
            }
        } else {
            ConversationLegacy.this.draft = null;
        }

        Toast.makeText(context, R.string.toast_draft, Toast.LENGTH_SHORT).show();
    }

    public int getType() {
        if (type == 0) {
            try {
                cursor = context.getContentResolver().query(CONVERSATIONS_CONTENT_PROVIDER, null, "_id=" + threadId, null, null);
                cursor.moveToFirst();
                type = cursor.getInt(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_TYPE));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return type;
    }

    private long[] getUnreadIds() {
        long[] ids = new long[0];

        try {
            cursor = context.getContentResolver().query(getUri(), new String[]{SmsHelper.COLUMN_ID}, SmsHelper.UNREAD_SELECTION, null, null);
            ids = new long[cursor.getCount()];
            cursor.moveToFirst();

            for (int i = 0; i < ids.length; i++) {
                ids[i] = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));
                cursor.moveToNext();
                Log.d(TAG, "Unread ID: " + ids[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ids;
    }

    public void markRead() {

        new Thread() {
            public void run() {

                long[] ids = getUnreadIds();
                if (ids.length > 0) {
                    new DefaultSmsHelper(context, R.string.not_default_mark_read).showIfNotDefault(null);

                    ContentValues cv = new ContentValues();
                    cv.put("read", true);
                    cv.put("seen", true);

                    for (long id : ids) {
                        context.getContentResolver().update(getUri(), cv, SmsHelper.COLUMN_ID + "=" + id, null);
                    }

                    NotificationManager.update(context);

                    UnreadBadgeService.update(context);
                }
            }
        }.start();
    }

    public void markUnread() {
        new DefaultSmsHelper(context, R.string.not_default_mark_unread).showIfNotDefault(null);

        try {
            cursor = context.getContentResolver().query(getUri(), MessageColumns.PROJECTION, null, null, SmsHelper.sortDateDesc);
            cursor.moveToFirst();

            MessageColumns.ColumnsMap columnsMap = new MessageColumns.ColumnsMap(cursor);
            MessageItem message = new MessageItem(context, cursor.getString(columnsMap.mColumnMsgType), cursor, columnsMap, null, true);

            if (message.isMe()) {
                while (cursor.moveToNext()) {
                    MessageItem message2 = new MessageItem(context, cursor.getString(columnsMap.mColumnMsgType), cursor, columnsMap, null, true);
                    if (!message2.isMe()) {
                        message = message2;
                        break;
                    }
                }
            }

            ContentValues cv = new ContentValues();
            cv.put("read", false);
            cv.put("seen", false);

            context.getContentResolver().update(message.mMessageUri, cv, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        NotificationManager.create(context);
    }

    public void delete() { //TODO do this using AsyncQueryHandler
        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SqliteWrapper.delete(context, context.getContentResolver(), getUri(), null, null);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(context, R.string.toast_conversation_deleted, Toast.LENGTH_SHORT).show();
            }
        }.execute((Void[]) null);
    }
}
