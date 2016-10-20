package com.moez.QKSMS.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.CursorObservable;
import com.moez.QKSMS.common.MessagingHelper;
import com.moez.QKSMS.common.SmsHelper;
import com.moez.QKSMS.common.SqliteWrapper;
import com.moez.QKSMS.common.google.DraftCache;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;

/**
 * Use this class (rather than Conversation) for marking conversations as read, and managing drafts.
 */
public class ConversationLegacy {
    private final String TAG = "ConversationLegacy";

    public static final Uri CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true");
    public static final Uri ADDRESSES_CONTENT_PROVIDER = Uri.parse("content://mms-sms/canonical-addresses");

    public static final int COLUMN_ADDRESSES_ADDRESS = 1;

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
    }

    public long getThreadId() {
        return threadId;
    }

    public Uri getUri() {
        return Uri.parse("content://mms-sms/conversations/" + getThreadId());
    }


    public String getName(boolean findIfNull) {
        if (name == null || name.trim().isEmpty()) {
            if (findIfNull) name = ContactHelper.getName(context, getAddress());
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
                        cursor = context.getContentResolver().query(SmsHelper.RECEIVED_MESSAGE_CONTENT_PROVIDER, new String[]{Telephony.Sms._ID}, "thread_id=" + threadId, null, SmsHelper.SORT_DATE_DESC);
                        cursor.moveToFirst();

                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID));
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
                recipient = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.ThreadsColumns.RECIPIENT_IDS));
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
                cursor = context.getContentResolver().query(SmsHelper.DRAFTS_CONTENT_PROVIDER, null, Telephony.Sms.THREAD_ID + "=" + threadId, null, null);
                cursor.moveToFirst();
                draft = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
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
        if (!hasDraft()) {
            return;
        }

        DraftCache.getInstance().setSavingDraft(true);
        DraftCache.getInstance().setDraftState(threadId, false);

        new CursorObservable(context, SmsHelper.DRAFTS_CONTENT_PROVIDER, null, Telephony.Sms.THREAD_ID + "=" + threadId, null, null)
                .map(cursor -> cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))
                .doOnCompleted(() -> DraftCache.getInstance().setSavingDraft(false))
                .subscribe(id -> MessagingHelper.deleteMessage(context, id));
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
                type = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return type;
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
