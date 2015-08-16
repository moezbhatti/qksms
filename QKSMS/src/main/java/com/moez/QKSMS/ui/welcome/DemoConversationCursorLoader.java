package com.moez.QKSMS.ui.welcome;

import android.content.Context;
import android.content.Loader;
import android.content.res.Resources;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.Telephony;
import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.messagelist.MessageColumns;

import java.util.ArrayList;

/**
 * A CursorLoader for demo conversations.
 */
public class DemoConversationCursorLoader extends Loader<Cursor> {

    private long mThreadId;
    private String mSelection;

    public static long THREAD_ID_WELCOME_SCREEN = -2;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     * @param threadId tells the loader which demo thread cursor to load
     */
    public DemoConversationCursorLoader(Context context, long threadId, String selection) {
        super(context);
        mThreadId = threadId;
        mSelection = selection;
    }

    /**
     * This method starts loading the cursor object. In this case, we don't need to do any
     * work on a background thread, so at the end of this method we can just call deliverResult.
     */
    @Override
    public void onStartLoading() {
        Resources res = getContext().getResources();

        // First, build a list of all results. Then, filter the results using mSelection and return
        // that list.
        ArrayList<MessageData> candidates = new ArrayList<MessageData>();

        if (THREAD_ID_WELCOME_SCREEN == mThreadId) {
            candidates = new ArrayList<MessageData>();
            long time = System.currentTimeMillis() - 50000;
            candidates.add(new MessageData(
                    "sms", 0, mThreadId, "QKSMS",
                    res.getString(R.string.welcome_quickreply_message_1),
                    time, time,
                    1, Message.RECEIVED, Telephony.Sms.STATUS_COMPLETE, 0, 0
            ));
            candidates.add(new MessageData(
                    "sms", 1, mThreadId, "QKSMS",
                    res.getString(R.string.welcome_quickreply_message_2),
                    System.currentTimeMillis(), System.currentTimeMillis(),
                    0, Message.RECEIVED, Telephony.Sms.STATUS_COMPLETE, 0, 0
            ));
        }

        // Build the actual list based on the selection.
        ArrayList<MessageData> data = new ArrayList<MessageData>();
        for (MessageData m : candidates) {
            if (!SmsHelper.UNREAD_SELECTION.equals(mSelection) || m.read == 0) {
                data.add(m);
            }
        }

        // Deliver the result cursor.
        deliverResult(new DemoConversationCursor(data));
    }

    private class MessageData {
        public static final int COLUMN_MSG_TYPE            = 0;
        public static final int COLUMN_ID                  = 1;
        public static final int COLUMN_THREAD_ID           = 2;
        public static final int COLUMN_SMS_ADDRESS         = 3;
        public static final int COLUMN_SMS_BODY            = 4;
        public static final int COLUMN_SMS_DATE            = 5;
        public static final int COLUMN_SMS_DATE_SENT       = 6;
        public static final int COLUMN_SMS_READ            = 7;
        public static final int COLUMN_SMS_TYPE            = 8;
        public static final int COLUMN_SMS_STATUS          = 9;
        public static final int COLUMN_SMS_LOCKED          = 10;
        public static final int COLUMN_SMS_ERROR_CODE      = 11;

        public String msg_type;
        public long id;
        public long thread_id;
        public String address;
        public String body;
        public long date;
        public long date_sent;
        public int read;
        public int sms_type;
        public int status;
        public int locked;
        public int error_code;

        public MessageData(String msg_type, long id, long thread_id, String address, String body,
                           long date, long date_sent, int read, int sms_type, int status,
                           int locked, int error_code) {

            this.msg_type = msg_type;   this.id = id;           this.thread_id = thread_id;
            this.address = address;     this.body = body;       this.date = date;
            this.date_sent = date_sent; this.read = read;       this.sms_type = sms_type;
            this.status = status;       this.locked = locked;   this.error_code = error_code;
        }
    }

    private class DemoConversationCursor extends AbstractCursor {
        public static final String TAG = "DemoConversationCursor";

        private ArrayList<MessageData> mMessageDataList;

        public DemoConversationCursor(ArrayList<MessageData> messageDataList) {
            mMessageDataList = messageDataList;
        }

        @Override
        public int getCount() {
            return mMessageDataList.size();
        }

        @Override
        public String[] getColumnNames() {
            return MessageColumns.PROJECTION;
        }

        @Override
        public String getString(int column) {
            MessageData data = mMessageDataList.get(mPos);
            switch (column) {
                case MessageData.COLUMN_MSG_TYPE:
                    return data.msg_type;
                case MessageData.COLUMN_SMS_ADDRESS:
                    return data.address;
                case MessageData.COLUMN_SMS_BODY:
                    return data.body;
            }
            return null;
        }

        @Override
        public short getShort(int column) {
            return 0;
        }

        @Override
        public int getInt(int column) {
            MessageData data = mMessageDataList.get(mPos);
            switch (column) {
                case MessageData.COLUMN_SMS_READ:
                    return data.read;
                case MessageData.COLUMN_SMS_TYPE:
                    return data.sms_type;
                case MessageData.COLUMN_SMS_STATUS:
                    return data.status;
                case MessageData.COLUMN_SMS_LOCKED:
                    return data.locked;
                case MessageData.COLUMN_SMS_ERROR_CODE:
                    return data.error_code;
            }
            return 0;
        }

        @Override
        public long getLong(int column) {
            MessageData data = mMessageDataList.get(mPos);
            switch (column) {
                case MessageData.COLUMN_ID:
                    return data.id;
                case MessageData.COLUMN_THREAD_ID:
                    return data.thread_id;
                case MessageData.COLUMN_SMS_DATE:
                    return data.date;
                case MessageData.COLUMN_SMS_DATE_SENT:
                    return data.date_sent;
            }
            return 0;
        }

        @Override
        public float getFloat(int column) {
            return 0;
        }

        @Override
        public double getDouble(int column) {
            return 0;
        }

        @Override
        public boolean isNull(int column) {
            return false;
        }
    }
}
