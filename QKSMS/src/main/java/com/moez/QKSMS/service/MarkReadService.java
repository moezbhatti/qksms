package com.moez.QKSMS.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.moez.QKSMS.common.MessagingHelper;

public class MarkReadService extends IntentService {

    public MarkReadService() {
        super("MarkReadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        long threadId = extras.getLong("thread_id");

        MessagingHelper.markConversationRead(this, threadId);
    }
}
