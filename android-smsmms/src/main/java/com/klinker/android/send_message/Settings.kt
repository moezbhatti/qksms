/*
 * Copyright 2013 Jacob Klinker
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

package com.klinker.android.send_message

import android.os.Build

/**
 * Class to house all of the settings that can be used to send a message
 */
class Settings @JvmOverloads constructor(
        // MMS options
        var mmsc: String? = "",
        var proxy: String? = "",
        var port: String? = "0",
        var agent: String? = "",
        var userProfileUrl: String? = "",
        var uaProfTagName: String? = "",

        // SMS options
        var deliveryReports: Boolean = false,
        var split: Boolean = false,
        var splitCounter: Boolean = false,
        var stripUnicode: Boolean = false,
        var signature: String? = null,
        var preText: String? = null,
        var sendLongAsMms: Boolean = true,
        var sendLongAsMmsAfter: Int = 3,
        subscriptionId: Int = DEFAULT_SUBSCRIPTION_ID) {

    // SIM options
    var subscriptionId: Int = subscriptionId
        set(value) {
            // we won't allow you to go away from the default if your device doesn't support it
            field = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1 || false) {
                DEFAULT_SUBSCRIPTION_ID
            } else {
                value
            }
        }

    companion object {
        const val DEFAULT_SUBSCRIPTION_ID = -1
    }
}
