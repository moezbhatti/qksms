package com.moez.QKSMS.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.transaction.SmsHelper;

import java.util.Calendar;

public class AutoDeleteReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoDeleteService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar last = Calendar.getInstance();
        last.setTimeInMillis(QKPreferences.getLong(QKPreference.LAST_AUTO_DELETE_CHECK));

        Calendar current = Calendar.getInstance();

        // Continue if the auto delete setting is enabled, and we haven't done a purge today
        if (QKPreferences.getBoolean(QKPreference.AUTO_DELETE) && (last.getTimeInMillis() == 0 ||
                current.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR) ||
                current.get(Calendar.YEAR) != last.get(Calendar.YEAR))) {
            Log.i(TAG, "Ready to delete old messages");
            QKPreferences.setLong(QKPreference.LAST_AUTO_DELETE_CHECK, System.currentTimeMillis());

            getAutoDeleteCount(context);
        } else {
            Log.i(TAG, "Not going to delete old messages");
        }
    }

    public static void setupAutoDeleteAlarm(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 3); // We want this service to run when the phone is not likely being used

        Intent intent = new Intent(context, AutoDeleteReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 9237, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pIntent);
    }

    private int getAutoDeleteCount(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(QKPreferences.getString(QKPreference.AUTO_DELETE_READ)));

        Cursor cursor = null;
        int count = 0;
        String selection = SmsHelper.COLUMN_DATE + "<=?";

        try {
            cursor = context.getContentResolver().query(
                    SmsHelper.SMS_CONTENT_PROVIDER,
                    new String[]{SmsHelper.COLUMN_ID, SmsHelper.COLUMN_DATE},
                    selection,
                    new String[]{String.valueOf(calendar.getTimeInMillis())},
                    null);

            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }
}
