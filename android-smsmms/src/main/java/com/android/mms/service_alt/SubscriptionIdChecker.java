
package com.android.mms.service_alt;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;
import com.google.android.mms.util_alt.SqliteWrapper;

class SubscriptionIdChecker {
    private static final String TAG = "SubscriptionIdChecker";

    private static SubscriptionIdChecker sInstance;
    private boolean mCanUseSubscriptionId = false;

    // I met a device which does not have Telephony.Mms.SUBSCRIPTION_ID event if it's API Level is 22.
    private void check(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Cursor c = null;
            try {
                c = SqliteWrapper.query(context, context.getContentResolver(),
                        Telephony.Mms.CONTENT_URI,
                        new String[]{Telephony.Mms.SUBSCRIPTION_ID}, null, null, null);
                if (c != null) {
                    mCanUseSubscriptionId = true;
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "SubscriptionIdChecker.check() fail");
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    static synchronized SubscriptionIdChecker getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SubscriptionIdChecker();
            sInstance.check(context);
        }
        return sInstance;
    }

    boolean canUseSubscriptionId() {
        return mCanUseSubscriptionId;
    }
}
