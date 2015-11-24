package com.moez.QKSMS.service;

import com.moez.QKSMS.mmssms.Message;
import com.moez.QKSMS.mmssms.Transaction;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.popup.QKReplyActivity;
import com.pushbullet.android.extension.MessagingExtension;

public class PushbulletService extends MessagingExtension {
    private final String TAG = "PushbulletService";

    @Override
    protected void onMessageReceived(String conversationIden, String body) {
        long threadId = Long.parseLong(conversationIden);
        ConversationLegacy conversation = new ConversationLegacy(getApplicationContext(), threadId);

        Transaction sendTransaction = new Transaction(getApplicationContext(), SmsHelper.getSendSettings(getApplicationContext()));
        Message message = new com.moez.QKSMS.mmssms.Message.Builder()
                .text(body)
                .address(conversation.getAddress())
                .build();
        message.setType(com.moez.QKSMS.mmssms.Message.TYPE_SMSMMS);
        sendTransaction.sendNewMessage(message, conversation.getThreadId());

        QKReplyActivity.dismiss(conversation.getThreadId());

        NotificationManager.update(getApplicationContext());
    }

    @Override
    protected void onConversationDismissed(String conversationIden) {
        long threadId = Long.parseLong(conversationIden);
        ConversationLegacy conversation = new ConversationLegacy(getApplicationContext(), threadId);
        conversation.markRead();
    }

}
