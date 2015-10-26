package com.moez.QKSMS.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import com.moez.QKSMS.R;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;

public class Message {
    private final String TAG = "Message";

    public static final int RECEIVED = 1;
    public static final int SENT = 2;
    public static final int DRAFT = 3;
    public static final int SENDING = 4;
    public static final int FAILED = 5;

    public static final Uri SMS_CONTENT_PROVIDER = Uri.parse("content://sms/");
    public static final Uri MMS_SMS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations/");
    public static final Uri SENT_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/sent");

    // ContentResolver columns
    static final Uri RECEIVED_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/inbox");
    private Context context;
    private ContactHelper contactHelper;
    private Uri uri;
    private long id;
    private long threadId;
    private String body;
    private String address;
    private String name;
    private long contactId;
    private Bitmap photoBitmap;
    private int read; // change to boolean

    public Message(Context context, long id) {
        this.context = context;
        this.id = id;

        contactHelper = new ContactHelper();

        uri = Uri.withAppendedPath(MMS_SMS_CONTENT_PROVIDER, "" + id);
    }

    public Message(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;

        contactHelper = new ContactHelper();

        Cursor cursor = context.getContentResolver().query(uri, new String[]{SmsHelper.COLUMN_ID}, null, null, null);
        cursor.moveToFirst();
        id = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));
        cursor.close();
    }

    public long getId() {
        return id;
    }

    public long getThreadId() {
        if (threadId == 0) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_THREAD_ID}, "_id=" + id, null, null);
                cursor.moveToFirst();
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_THREAD_ID));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return threadId;
    }

    public boolean isMms() {
        boolean isMms = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MMS_SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_MMS}, "_id=" + id, null, null);
            cursor.moveToFirst();
            isMms = "application/vnd.wap.multipart.related".equals(cursor.getString(cursor.getColumnIndex(SmsHelper.COLUMN_MMS)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isMms;
    }

    public String getAddress() {
        Cursor cursor = null;
        if (address == null) {
            try {
                cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_ADDRESS}, "_id=" + id, null, null);
                cursor.moveToFirst();
                address = cursor.getString(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ADDRESS));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return address;
    }

    public String getName() {
        if (name == null) name = contactHelper.getName(context, getAddress());
        return name;
    }

    public String getBody() {
        if (body == null) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_BODY}, "_id=" + id, null, null);
                cursor.moveToFirst();
                body = cursor.getString(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_BODY));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return body;
    }

    public long getContactId() {
        if (contactId == 0) contactId = contactHelper.getId(context, getAddress());
        return contactId;
    }

    public Bitmap getPhotoBitmap() {
        if (photoBitmap == null)
            photoBitmap = contactHelper.getBitmap(context, getContactId());
        return photoBitmap;
    }

    public void markSeen() {
        ContentValues cv = new ContentValues();
        cv.put("seen", true);

        if (isMms()) {
            context.getContentResolver().update(Uri.parse("content://mms/" + getId()), cv, null, null);
        } else {
            context.getContentResolver().update(Uri.parse("content://sms/" + getId()), cv, null, null);
        }
    }

    public void markRead() {
        ContentValues cv = new ContentValues();
        cv.put("read", true);
        cv.put("seen", true);

        if (isMms()) {
            context.getContentResolver().update(Uri.parse("content://mms/" + getId()), cv, null, null);
        } else {
            context.getContentResolver().update(Uri.parse("content://sms/" + getId()), cv, null, null);
        }
    }

    public void delete() {
        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);

        try {
            if (isMms()) {
                context.getContentResolver().delete(Uri.parse("content://mms/" + getId()), null, null);
            } else {
                context.getContentResolver().delete(Uri.parse("content://sms/" + getId()), null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
