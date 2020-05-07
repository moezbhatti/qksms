/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.mms.util_alt;

import android.content.Context;
import android.drm.DrmManagerClient;
import timber.log.Timber;

public class DownloadDrmHelper {


    /** The MIME type of special DRM files */
    public static final String MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message";

    /** The extensions of special DRM files */
    public static final String EXTENSION_DRM_MESSAGE = ".dm";

    public static final String EXTENSION_INTERNAL_FWDL = ".fl";

    /**
     * Checks if the Media Type is a DRM Media Type
     *
     * @param mimetype Media Type to check
     * @return True if the Media Type is DRM else false
     */
    public static boolean isDrmMimeType(Context context, String mimetype) {
        boolean result = false;
        if (context != null) {
            try {
                DrmManagerClient drmClient = new DrmManagerClient(context);
                if (drmClient != null && mimetype != null && mimetype.length() > 0) {
                    result = drmClient.canHandle("", mimetype);
                }
            } catch (IllegalArgumentException e) {
                Timber.w("DrmManagerClient instance could not be created, context is Illegal.");
            } catch (IllegalStateException e) {
                Timber.w("DrmManagerClient didn't initialize properly.");
            }
        }
        return result;
    }

    /**
     * Checks if the Media Type needs to be DRM converted
     *
     * @param mimetype Media type of the content
     * @return True if convert is needed else false
     */
    public static boolean isDrmConvertNeeded(String mimetype) {
        return MIMETYPE_DRM_MESSAGE.equals(mimetype);
    }

    /**
     * Modifies the file extension for a DRM Forward Lock file NOTE: This
     * function shouldn't be called if the file shouldn't be DRM converted
     */
    public static String modifyDrmFwLockFileExtension(String filename) {
        if (filename != null) {
            int extensionIndex;
            extensionIndex = filename.lastIndexOf(".");
            if (extensionIndex != -1) {
                filename = filename.substring(0, extensionIndex);
            }
            filename = filename.concat(EXTENSION_INTERNAL_FWDL);
        }
        return filename;
    }

    /**
     * Gets the original mime type of DRM protected content.
     *
     * @param context The context
     * @param path Path to the file
     * @param containingMime The current mime type of of the file i.e. the
     *            containing mime type
     * @return The original mime type of the file if DRM protected else the
     *         currentMime
     */
    public static String getOriginalMimeType(Context context, String path, String containingMime) {
        String result = containingMime;
        DrmManagerClient drmClient = new DrmManagerClient(context);
        try {
            if (drmClient.canHandle(path, null)) {
                result = drmClient.getOriginalMimeType(path);
            }
        } catch (IllegalArgumentException ex) {
            Timber.w("Can't get original mime type since path is null or empty string.");
        } catch (IllegalStateException ex) {
            Timber.w("DrmManagerClient didn't initialize properly.");
        }
        return result;
    }
}
