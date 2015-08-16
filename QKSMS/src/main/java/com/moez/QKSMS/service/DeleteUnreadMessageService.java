package com.moez.QKSMS.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;

import java.util.ArrayList;

// We have this in a service beacause depending on the number of messages and the device, it can be
// slow. We don't want to leave the QK Reply window open while it's marking as read, so we let a
// service execute the code and we can shut down the activity. Otherwise it'll look like the app it
// just lagging.
public class DeleteUnreadMessageService extends IntentService {

    public static String EXTRA_THREAD_URI = "threadUri";

    public DeleteUnreadMessageService() {
        super("DeleteUnreadMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri threadUri = intent.getParcelableExtra(EXTRA_THREAD_URI);

        // The messages are marked as read before deleting due to an issue with the android content
        // provider. When the message is deleted, it doesn't notify the conversations table. So if
        // there is an unread message, and it's deleted, then the conversation will remain unread.
        // Then even when you try to mark it as read and it iterates over unread messages to mark
        // them read, it won't be able to find any because they were deleted, leaving the
        // conversation "stuck" as unread. The only way to un-stick it is to receive a new message
        // in the conversation and mark that as read. Marking them read before deleting them solves
        // this problem.
        ArrayList<Message> messages = SmsHelper.getUnreadMessagesLegacy(this, threadUri);
        for (Message message : messages) {
            message.markRead();
            message.delete();
        }
        NotificationManager.update(this);
    }
}
