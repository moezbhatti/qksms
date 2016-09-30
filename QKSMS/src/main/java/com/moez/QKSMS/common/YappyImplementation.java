package com.moez.QKSMS.common;

import android.content.Context;
import android.util.Log;
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
        MessagingHelper.deleteMessage(context, id);
    }

    @Override
    public void DeleteMMSMessage(Context context, int id) {
        MessagingHelper.deleteMessage(context, id);
    }

    @Override
    public void SendMMS(Context context, String[] recipients, MMSPart[] parts, String body, boolean save, boolean send) {
        Log.d(TAG, "SendMMS");
        MessagingHelper.sendMessage(context, recipients, body, null);
    }

    @Override
    public void SendSMS(Context context, String[] recipients, String body, boolean send) {
        Log.d(TAG, "SendSMS");
        MessagingHelper.sendMessage(context, recipients, body, null);
    }
}
