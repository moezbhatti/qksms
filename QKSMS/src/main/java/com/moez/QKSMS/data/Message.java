package com.moez.QKSMS.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Telephony;
import com.moez.QKSMS.common.SmsHelper;

public class Message {
    private final String TAG = "Message";

    public static final int RECEIVED = 1;
    public static final int SENT = 2;
    public static final int DRAFT = 3;
    public static final int SENDING = 4;
    public static final int FAILED = 5;

    private Context context;
    private long id;
    private long threadId;
    private String body;
    private String address;
    private String name;
    private long contactId;
    private Bitmap photoBitmap;

    public Message(Context context, long id) {
        this.context = context;
        this.id = id;
    }

    public Message(Context context, Uri uri) {
        this.context = context;

        Cursor cursor = context.getContentResolver().query(uri, new String[]{Telephony.Sms._ID}, null, null, null);
        cursor.moveToFirst();
        id = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID));
        cursor.close();
    }

    public long getId() {
        return id;
    }

    public long getThreadId() {
        if (threadId == 0) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(SmsHelper.SMS_CONTENT_PROVIDER, new String[]{Telephony.Sms.THREAD_ID}, "_id=" + id, null, null);
                cursor.moveToFirst();
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID));
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
            cursor = context.getContentResolver().query(SmsHelper.MMS_SMS_CONTENT_PROVIDER, new String[]{Telephony.Mms.CONTENT_TYPE}, "_id=" + id, null, null);
            cursor.moveToFirst();
            isMms = "application/vnd.wap.multipart.related".equals(cursor.getString(cursor.getColumnIndex(Telephony.Mms.CONTENT_TYPE)));
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
                cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, new String[]{Telephony.Sms.ADDRESS}, "_id=" + id, null, null);
                cursor.moveToFirst();
                address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
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
        if (name == null) name = ContactHelper.getName(context, getAddress());
        return name;
    }

    public String getBody() {
        if (body == null) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, new String[]{Telephony.Sms.BODY}, "_id=" + id, null, null);
                cursor.moveToFirst();
                body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
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
        if (contactId == 0) contactId = ContactHelper.getId(context, getAddress());
        return contactId;
    }

    public Bitmap getPhotoBitmap() {
        if (photoBitmap == null)
            photoBitmap = ContactHelper.getBitmap(context, getContactId());
        return photoBitmap;
    }
}
