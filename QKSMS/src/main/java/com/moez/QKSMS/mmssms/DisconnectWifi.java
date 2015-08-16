/*
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

package com.moez.QKSMS.mmssms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

public class DisconnectWifi extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {
        // if wifi tries to connect while we are sending an MMS, disable it until that message is done sending
        WifiManager wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        if (!intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE).toString().equals(SupplicantState.SCANNING))
            wifi.disconnect();
    }
}
