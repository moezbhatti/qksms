/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import android.os.Build;
import android.provider.Telephony.Mms;
import com.klinker.android.logger.Log;

import com.android.mms.logs.LogTag;
import com.klinker.android.send_message.Utils;

/**
 * MmsSystemEventReceiver receives the
 * and performs a series of operations which may include:
 * <ul>
 * <li>Show/hide the icon in notification area which is used to indicate
 * whether there is new incoming message.</li>
 * <li>Resend the MM's in the outbox.</li>
 * </ul>
 */
public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static final String TAG = LogTag.TAG;
    private static ConnectivityManager mConnMgr = null;

    public static void wakeUpService(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "wakeUpService: start transaction service ...");
            }

            context.startService(new Intent(context, TransactionService.class));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Intent received: " + intent);
        }

        if (!Utils.isDefaultSmsApp(context)) {
            Log.v(TAG, "not default sms app, cancelling");
            return;
        }

        String action = intent.getAction();
        if (action.equals(Mms.Intents.CONTENT_CHANGED_ACTION)) {
            Uri changed = (Uri) intent.getParcelableExtra(Mms.Intents.DELETED_CONTENTS);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (mConnMgr == null) {
                mConnMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }

            if (Utils.isMmsOverWifiEnabled(context)) {
                NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if ((niWF != null) && (niWF.isConnected())) {
                    Log.v(TAG, "TYPE_WIFI connected");
                    wakeUpService(context);
                }
            } else {
                boolean mobileDataEnabled = Utils.isMobileDataEnabled(context);
                if (!mobileDataEnabled) {
                    Log.v(TAG, "mobile data turned off, bailing");
                    //Utils.setMobileDataEnabled(context, true);
                    return;
                }
                NetworkInfo mmsNetworkInfo = mConnMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                if (mmsNetworkInfo == null) {
                    return;
                }
                boolean available = mmsNetworkInfo.isAvailable();
                boolean isConnected = mmsNetworkInfo.isConnected();

                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "TYPE_MOBILE_MMS available = " + available +
                            ", isConnected = " + isConnected);
                }

                // Wake up transact service when MMS data is available and isn't connected.
                if (available && !isConnected) {
                    wakeUpService(context);
                }
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
