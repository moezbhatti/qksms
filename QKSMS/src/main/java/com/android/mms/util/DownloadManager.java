/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduPersister;
import com.moez.QKSMS.R;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    public static final int DEFERRED_MASK           = 0x04;

    public static final int STATE_UNKNOWN           = 0x00;
    public static final int STATE_UNSTARTED         = 0x80;
    public static final int STATE_DOWNLOADING       = 0x81;
    public static final int STATE_TRANSIENT_FAILURE = 0x82;
    public static final int STATE_PERMANENT_FAILURE = 0x87;
    public static final int STATE_PRE_DOWNLOADING   = 0x88;
    public static final int STATE_SKIP_RETRYING     = 0x89;

    private final Context mContext;
    private final Handler mHandler;
    private final SharedPreferences mPreferences;
    private boolean mAutoDownload;

    private static DownloadManager sInstance;

    private DownloadManager(Context context) {
        mContext = context;
        mHandler = new Handler();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mAutoDownload = getAutoDownloadState(context);
        if (LOCAL_LOGV) Log.v(TAG, "mAutoDownload ------> " + mAutoDownload);
    }

    public boolean isAuto() {
        return mAutoDownload;
    }

    public static void init(Context context) {
        sInstance = new DownloadManager(context);
    }

    public static DownloadManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }

    static boolean getAutoDownloadState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_download_mms", true);
    }

    static boolean getAutoDownloadState(SharedPreferences prefs, boolean roaming) {
        boolean autoDownload = prefs.getBoolean("auto_download_mms", true);

        if (LOCAL_LOGV) Log.v(TAG, "auto download without roaming -> " + autoDownload);

        if (autoDownload) {
            boolean alwaysAuto = true;

            if (LOCAL_LOGV) Log.v(TAG, "auto download during roaming -> " + alwaysAuto);

            if (!roaming || alwaysAuto) {
                return true;
            }
        }
        return false;
    }

    static boolean isRoaming() {
        return false;
    }

    public void markState(final Uri uri, int state) {
        // Notify user if the message has expired.
        try {
            NotificationInd nInd = (NotificationInd) PduPersister.getPduPersister(mContext)
                    .load(uri);
            if ((nInd.getExpiry() < System.currentTimeMillis() / 1000L)
                    && (state == STATE_DOWNLOADING || state == STATE_PRE_DOWNLOADING)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(mContext, R.string.service_message_not_found,
                                Toast.LENGTH_LONG).show();
                    }
                });
                SqliteWrapper.delete(mContext, mContext.getContentResolver(), uri, null, null);
                return;
            }
        } catch(MmsException e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }

        // Notify user if downloading permanently failed.
        if (state == STATE_PERMANENT_FAILURE) {
            /*
             * TODO: Until we can generate a better notification message, don't do this.
            try {
                final String message = getMessage(uri);
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MmsException e) {
                Log.e(TAG, e.getMessage(), e);
            }
             */
        } else if (!mAutoDownload) {
            state |= DEFERRED_MASK;
        }

        // Use the STATUS field to store the state of downloading process
        // because it's useless for M-Notification.ind.
        ContentValues values = new ContentValues(1);
        values.put("st", state);
        SqliteWrapper.update(mContext, mContext.getContentResolver(),
                    uri, values, null, null);
    }

    public void showErrorCodeToast(int errorStr) {
        final int errStr = errorStr;
        mHandler.post(new Runnable() {
            public void run() {
                try {
                    Toast.makeText(mContext, errStr, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG,"Caught an exception in showErrorCodeToast");
                }
            }
        });
    }

    private String getMessage(Uri uri) throws MmsException {
        NotificationInd ind = (NotificationInd) PduPersister
                .getPduPersister(mContext).load(uri);

        EncodedStringValue v = ind.getSubject();
        String subject = (v != null) ? v.getString()
                : mContext.getString(R.string.no_subject);

        // TODO: Actually getConversation the sender, or at least the phone number here
        String from = mContext.getString(R.string.unknown_sender);

        return mContext.getString(R.string.dl_failure_notification, subject, from);
    }

    public int getState(Uri uri) {
        Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                            uri, new String[] {"st"}, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0) & ~DEFERRED_MASK;
                }
            } finally {
                cursor.close();
            }
        }
        return STATE_UNSTARTED;
    }
}
