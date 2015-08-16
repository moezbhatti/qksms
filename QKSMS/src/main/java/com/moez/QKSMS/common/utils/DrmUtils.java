/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.moez.QKSMS.common.utils;

import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.net.Uri;
import android.util.Log;

import com.moez.QKSMS.QKSMSApp;

public class DrmUtils {
    private static final String TAG = "DrmUtils";

    /** The MIME type of special DRM files */
    private static final String EXTENSION_ANDROID_FWDL = ".fl";

    private DrmUtils() {
    }

    public static String getConvertExtension(String mimetype) {
        return EXTENSION_ANDROID_FWDL;
    }

    public static boolean isDrmType(String mimeType) {
        boolean result = false;
        DrmManagerClient drmManagerClient = QKSMSApp.getApplication().getDrmManagerClient();
        if (drmManagerClient != null) {
            try {
                if (drmManagerClient.canHandle("", mimeType)) {
                    result = true;
                }
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "canHandle called with wrong parameters");
            } catch (IllegalStateException ex) {
                Log.w(TAG, "DrmManagerClient didn't initialize properly");
            }
        }
        return result;
    }

    /**
     * Check if content may be forwarded according to DRM
     *
     * @param uri Uri to content
     * @return true if the content may be forwarded
     */
    public static boolean haveRightsForAction(Uri uri, int action) {
        DrmManagerClient drmManagerClient = QKSMSApp.getApplication().getDrmManagerClient();

        try {
            // first check if the URI is registered as DRM in DRM-framework
            if (drmManagerClient.canHandle(uri.toString(), null)) {
                int check = drmManagerClient.checkRightsStatus(uri.toString(), action);
                return (check == DrmStore.RightsStatus.RIGHTS_VALID);
            }
        } catch (Exception e) {
            // Ignore exception and assume it is OK to forward file.
        } finally {
        }
        return true;
    }
}
