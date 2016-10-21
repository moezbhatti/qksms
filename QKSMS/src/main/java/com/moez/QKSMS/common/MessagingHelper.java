package com.moez.QKSMS.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Telephony;
import com.moez.QKSMS.mmssms.Message;
import com.moez.QKSMS.mmssms.Transaction;
import com.moez.QKSMS.service.UnreadBadgeService;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.messagelist.MessageItem;
import rx.Observable;
import rx.schedulers.Schedulers;

public class MessagingHelper {
    private static final String TAG = "MessagingHelper";

    public static void sendMessage(Context context, String recipient, String body, Bitmap attachment) {
        sendMessage(context, new String[]{recipient}, body, attachment);
    }

    public static void sendMessage(Context context, String[] recipients, String body, Bitmap attachment) {
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings());
        Message message = new Message(body, recipients);

        if (attachment != null) {
            message.setImage(attachment);
            message.setSubject(body);
        }

        if (!body.equals("")) {
            sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
        }
    }

    public static void markMessageUnread(Context context, long id) {
        ContentValues cv = new ContentValues();
        cv.put("read", false);
        cv.put("seen", false);
        updateMessage(context, id, cv);
    }

    public static void markMessageRead(Context context, long id) {
        ContentValues cv = new ContentValues();
        cv.put("read", true);
        cv.put("seen", true);
        updateMessage(context, id, cv);
    }

    public static void markMessageSeen(Context context, long id) {
        ContentValues cv = new ContentValues();
        cv.put("seen", true);
        updateMessage(context, id, cv);
    }

    private static void updateMessage(Context context, long messageId, ContentValues cv) {
        Observable.just(messageId).subscribeOn(Schedulers.io()).subscribe(id -> {
            if (MessagingHelper.isMms(context, id)) {
                context.getContentResolver().update(Uri.parse("content://mms/" + id), cv, null, null);
            } else {
                context.getContentResolver().update(Uri.parse("content://sms/" + id), cv, null, null);
            }
        });
    }

    public static void deleteMessage(Context context, long messageId) {
        Observable.just(messageId).subscribeOn(Schedulers.io()).subscribe(id -> {
            try {
                if (MessagingHelper.isMms(context, id)) {
                    context.getContentResolver().delete(Uri.parse("content://mms/" + id), null, null);
                } else {
                    context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void deleteFailedMessages(Context context, long threadId) {
        CursorObservable.from(context, SmsHelper.SMS_CONTENT_PROVIDER, new String[]{Telephony.Sms.THREAD_ID, Telephony.Sms._ID},
                SmsHelper.FAILED_SELECTION, null, null)
                .filter(cursor -> cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)) == threadId)
                .map(cursor -> cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))
                .subscribe(messageId -> deleteMessage(context, messageId));
    }

    public static void markConversationRead(Context context, long id) {
        Uri uri = Uri.parse("content://mms-sms/conversations/" + id);

        CursorObservable.from(context, uri, new String[]{Telephony.Sms._ID}, SmsHelper.UNREAD_SELECTION, null, null)
                .doOnCompleted(() -> {
                    NotificationManager.update(context);
                    UnreadBadgeService.update(context);
                })
                .map(cursor -> cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)))
                .subscribe(messageId -> markMessageRead(context, messageId));
    }

    public static void markConversationUnread(Context context, long id) {
        Uri uri = Uri.parse("content://mms-sms/conversations/" + id);
        final MessageColumns.ColumnsMap[] columnsMap = {null};

        CursorObservable.from(context, uri, MessageColumns.PROJECTION, null, null, SmsHelper.SORT_DATE_DESC)
                .doOnCompleted(() -> {
                    NotificationManager.create(context);
                    UnreadBadgeService.update(context);
                })
                .map(cursor -> {
                    if (columnsMap[0] == null) columnsMap[0] = new MessageColumns.ColumnsMap(cursor);
                    return new MessageItem(context, cursor.getString(columnsMap[0].mColumnMsgType), cursor, columnsMap[0], null, true);
                })
                .filter(message -> !message.isMe())
                .first()
                .subscribe(message -> markMessageUnread(context, message.mMsgId));
    }

    public static boolean isMms(Context context, long id) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, new String[]{Telephony.Mms.CONTENT_TYPE}, "_id=" + id, null, null);
            cursor.moveToFirst();
            return "application/vnd.wap.multipart.related".equals(cursor.getString(cursor.getColumnIndex(Telephony.Mms.CONTENT_TYPE)));
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
