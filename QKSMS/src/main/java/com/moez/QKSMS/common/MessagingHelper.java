package com.moez.QKSMS.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Telephony;
import com.moez.QKSMS.mmssms.Message;
import com.moez.QKSMS.mmssms.Transaction;

public class MessagingHelper {

    public static void sendMessage(Context context, String recipient, String body, Bitmap attachment) {
        sendMessage(context, new String[]{recipient}, body, attachment);
    }

    public static void sendMessage(Context context, String[] recipients, String body, Bitmap attachment) {
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings(context));
        Message message = new Message(body, recipients);

        if (attachment != null) {
            message.setImage(attachment);
            message.setSubject(body);
        }

        if (!body.equals("")) {
            sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
        }
    }

    public static void markMessageRead(Context context, long id) {
        ContentValues cv = new ContentValues();
        cv.put("read", true);
        cv.put("seen", true);

        if (MessagingHelper.isMms(context, id)) {
            context.getContentResolver().update(Uri.parse("content://mms/" + id), cv, null, null);
        } else {
            context.getContentResolver().update(Uri.parse("content://sms/" + id), cv, null, null);
        }
    }

    public static void markMessageSeen(Context context, long id) {
        ContentValues cv = new ContentValues();
        cv.put("seen", true);

        if (MessagingHelper.isMms(context, id)) {
            context.getContentResolver().update(Uri.parse("content://mms/" + id), cv, null, null);
        } else {
            context.getContentResolver().update(Uri.parse("content://sms/" + id), cv, null, null);
        }

    }

    public static void deleteMessage(Context context, long id) {
        try {
            if (MessagingHelper.isMms(context, id)) {
                context.getContentResolver().delete(Uri.parse("content://mms/" + id), null, null);
            } else {
                context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMms(Context context, long id) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, new String[]{SmsHelper.COLUMN_MMS}, "_id=" + id, null, null);
            cursor.moveToFirst();
            return "application/vnd.wap.multipart.related".equals(cursor.getString(cursor.getColumnIndex(SmsHelper.COLUMN_MMS)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }
}
