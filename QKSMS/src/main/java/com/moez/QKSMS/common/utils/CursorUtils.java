package com.moez.QKSMS.common.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.transaction.SmsHelper;

public class CursorUtils {
    public static final String TAG = "CursorUtils";

    /**
     * Returns true if the cursor is non-null and not closed.
     *
     * @param cursor
     * @return
     */
    public static boolean isValid(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }

    public static void outputColumns(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return;

        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            Log.d(TAG, "Column " + cursor.getColumnName(i) + ": " + cursor.getString(i));
        }
        Log.d(TAG, "------------------------------------------------------------");
    }

    public static void prepareEmulator(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Integer.parseInt(telephonyManager.getDeviceId()) > 0) return;

        try {
            context.getContentResolver().delete(Uri.parse("content://sms/"), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ContentResolver contentResolver = context.getContentResolver();

        String[][] messages = new String[][]{
                {"4165254009", "Why are you texting myself?", "1399856640", "" + Message.RECEIVED}, // address, body, date, type
                {"4166485592", "These popups are so handy!", "1400079840", "" + Message.RECEIVED}};

        for (int i = 0; i < messages.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put("address", messages[i][0]);
            cv.put("body", messages[i][1]);
            cv.put("date", messages[i][2]);
            cv.put("date_sent", messages[i][2]);
            cv.put("type", messages[i][3]);
            contentResolver.insert(SmsHelper.SMS_CONTENT_PROVIDER, cv);
        }


        /*for (int i = 0; i < messages.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put("date", messages[i][2]);
            contentResolver.update(SmsHelper.CONVERSATIONS_CONTENT_PROVIDER, cv, "snippit=" + messages[i][1], null);
        }*/
    }
}
