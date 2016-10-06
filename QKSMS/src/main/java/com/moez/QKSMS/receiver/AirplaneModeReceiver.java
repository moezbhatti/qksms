package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import com.moez.QKSMS.common.MessagingHelper;
import com.moez.QKSMS.common.SmsHelper;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.messagelist.MessageItem;

/**
 * Listen for changes to the Airplane Mode status, so that we can attempt to re-send failed messages
 * once we have signal
 */
public class AirplaneModeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // If we're going into airplane mode, no need to do anything
        if (intent.getBooleanExtra("state", true)) {
            return;
        }

        // Cursor to find the conversations that contain failed messages
        Cursor conversationCursor = context.getContentResolver().query(
                SmsHelper.CONVERSATIONS_CONTENT_PROVIDER,
                Conversation.ALL_THREADS_PROJECTION,
                Conversation.FAILED_SELECTION, null,
                SmsHelper.SORT_DATE_DESC
        );

        // Loop through each of the conversations
        while (conversationCursor.moveToNext()) {
            Uri uri = ContentUris.withAppendedId(SmsHelper.MMS_SMS_CONTENT_PROVIDER, conversationCursor.getLong(Conversation.ID));

            // Find the failed messages within the conversation
            Cursor cursor = context.getContentResolver().query(uri, MessageColumns.PROJECTION,
                    SmsHelper.FAILED_SELECTION, null, SmsHelper.SORT_DATE_ASC);

            // Map the cursor row to a MessageItem, then re-send it
            MessageColumns.ColumnsMap columnsMap = new MessageColumns.ColumnsMap(cursor);
            while (cursor.moveToNext()) {
                MessageItem message = new MessageItem(context, cursor.getString(columnsMap.mColumnMsgType), cursor, columnsMap, null, true);
                sendSms(context, message);
            }
            cursor.close();
        }

        conversationCursor.close();
    }

    private void sendSms(Context context, MessageItem messageItem) {
        context.getContentResolver().delete(messageItem.mMessageUri, null, null);
        MessagingHelper.sendMessage(context, messageItem.mAddress, messageItem.mBody, null);
    }
}
