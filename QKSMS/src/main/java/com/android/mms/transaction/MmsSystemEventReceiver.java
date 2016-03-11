/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.moez.QKSMS.mmssms.Utils;

/**
 * MmsSystemEventReceiver receives the
 * {@link android.content.intent.ACTION_BOOT_COMPLETED},
 * {@link com.android.internal.telephony.TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED}
 * and performs a series of operations which may include:
 * <ul>
 * <li>Show/hide the icon in notification area which is used to indicate
 * whether there is new incoming message.</li>
 * <li>Resend the MM's in the outbox.</li>
 * </ul>
 */
public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static final String TAG = "MmsSystemEventReceiver";
    private static ConnectivityManager mConnMgr = null;
    private static final boolean LOCAL_LOGV = false;

    public static void wakeUpService(Context context) {
        if (LOCAL_LOGV) Log.v(TAG, "wakeUpService: start transaction service ...");

        Intent service  = new Intent(
                TransactionService.HANDLE_PENDING_TRANSACTIONS_ACTION, null, context,
                TransactionService.class
        );
        context.startService(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LOCAL_LOGV) Log.v(TAG, "Intent received: " + intent);

        String action = intent.getAction();
        if (action.equals("android.intent.action.CONTENT_CHANGED")) {
            intent.getParcelableExtra("deleted_contents");
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (mConnMgr == null) {
                mConnMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            boolean mobileDataEnabled = Utils.isMobileDataEnabled(context);

            if (!mobileDataEnabled) {
                if (LOCAL_LOGV) Log.v(TAG, "mobile data turned off, bailing");
                return;
            }
            NetworkInfo mmsNetworkInfo = mConnMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
            boolean available = mmsNetworkInfo.isAvailable();
            boolean isConnected = mmsNetworkInfo.isConnected();

            if (LOCAL_LOGV) Log.v(TAG, "TYPE_MOBILE_MMS available = " + available +
                           ", isConnected = " + isConnected);

            // Wake up transact service when MMS data is available and isn't connected.
            if (available && !isConnected) {
                wakeUpService(context);
            }
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // We should check whether there are unread incoming
            // messages in the Inbox and then update the notification icon.
            // Called on the UI thread so don't block.

            // Scan and send pending Mms once after boot completed since
            // ACTION_ANY_DATA_CONNECTION_STATE_CHANGED wasn't registered in a whole life cycle
            wakeUpService(context);
        }
    }
}
