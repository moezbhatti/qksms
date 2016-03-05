/*
 * Copyright (C) 2013 Samsung System LSI
 * Copyright (C) 2015 QK Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * MmsPushOutboxMessages listens for MMS_SEND_OUTBOX_MSG intent .
 * {@link android.intent.action.MMS_SEND_OUTBOX_MSG},
 * and wakes up the mms service when it receives it.
 * This will tricker the mms service to send any messages stored
 * in the outbox.
 */
public class MmsPushOutboxMessages extends BroadcastReceiver {
    private static final String INTENT_MMS_SEND_OUTBOX_MSG = "android.intent.action.MMS_SEND_OUTBOX_MSG";
    private static final String TAG = "MmsPushOutboxMessages";
    private static final boolean LOCAL_LOGV = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LOCAL_LOGV) Log.v(TAG, "Received the MMS_SEND_OUTBOX_MSG intent: " + intent);

        String action = intent.getAction();
        if(action.equalsIgnoreCase(INTENT_MMS_SEND_OUTBOX_MSG)){
            if (LOCAL_LOGV) Log.v(TAG, "Now waking up the MMS service");
        }
    }

}
