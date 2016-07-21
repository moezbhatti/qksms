package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.google.android.mms.MmsException;
import com.moez.QKSMS.mmssms.Message;
import com.moez.QKSMS.mmssms.Transaction;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.messagelist.MessageItem;

public class AirplaneModeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(
                SmsHelper.MMS_SMS_CONTENT_PROVIDER,
                MessageColumns.PROJECTION,
                SmsHelper.FAILED_SELECTION,
                null, SmsHelper.sortDateDesc
        );

        if (cursor.moveToFirst()) {
            MessageColumns.ColumnsMap columnsMap = new MessageColumns.ColumnsMap(cursor);
            for (int i = 0; i < cursor.getCount(); i++) {
                try {
                    MessageItem message = new MessageItem(context, cursor.getString(columnsMap.mColumnMsgType), cursor, columnsMap, null, true);
                    sendSms(context, message);
                } catch (MmsException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
    }

    private void sendSms(Context context, MessageItem messageItem) {
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings(context));

        Message message = new Message(messageItem.mBody, messageItem.mAddress);
        message.setType(Message.TYPE_SMSMMS);

        sendTransaction.sendNewMessage(message, 0);
    }
}
