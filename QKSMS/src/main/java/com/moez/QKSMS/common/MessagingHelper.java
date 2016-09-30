package com.moez.QKSMS.common;

import android.content.Context;
import android.graphics.Bitmap;
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
}
