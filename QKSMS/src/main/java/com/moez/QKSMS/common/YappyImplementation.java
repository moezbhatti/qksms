package com.moez.QKSMS.common;

import android.content.Context;
import android.util.Log;

import com.moez.QKSMS.mmssms.Message;
import com.moez.QKSMS.mmssms.Transaction;
import com.mariussoft.endlessjabber.sdk.IEndlessJabberImplementation;
import com.mariussoft.endlessjabber.sdk.MMSPart;
import com.moez.QKSMS.data.ConversationLegacy;

public class YappyImplementation implements IEndlessJabberImplementation {
    private final String TAG = "EndlessJabber";

    public YappyImplementation() {

    }

    @Override
    public void UpdateReadMessages(Context context, long time, int threadId) {
        Log.d(TAG, "UpdateReadMessages");
        new ConversationLegacy(context, threadId).markRead();
    }

    @Override
    public void DeleteThread(Context context, int threadId) {
        Log.d(TAG, "DeleteThread");
        new ConversationLegacy(context, threadId).delete();
    }

    @Override
    public void DeleteSMSMessage(Context context, int id) {
        new com.moez.QKSMS.data.Message(context, id).delete();
    }

    @Override
    public void DeleteMMSMessage(Context context, int id) {
        new com.moez.QKSMS.data.Message(context, id).delete();
    }

    @Override
    public void SendMMS(Context context, String[] recipients, MMSPart[] parts, String subject, boolean save, boolean send) {
        Log.d(TAG, "SendMMS");
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings(context));

        Message message = new Message();
        message.setType(com.moez.QKSMS.mmssms.Message.TYPE_SMSMMS);
        message.setAddresses(recipients);
        message.setSubject(subject);
        message.setSave(save);

        sendTransaction.sendNewMessage(message, 0);
    }

    @Override
    public void SendSMS(Context context, String[] recipients, String body, boolean send) {
        Log.d(TAG, "SendSMS");
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings(context));

        Message message = new Message();
        message.setType(com.moez.QKSMS.mmssms.Message.TYPE_SMSMMS);
        message.setAddresses(recipients);
        message.setText(body);

        sendTransaction.sendNewMessage(message, 0);
    }
}
