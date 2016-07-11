package com.moez.QKSMS.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.transaction.SmsHelper;

import java.util.Calendar;

public class DeleteOldMessagesService extends IntentService {
    private static final String TAG = "DeleteOldMessages";

    public DeleteOldMessagesService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar last = Calendar.getInstance();
        last.setTimeInMillis(QKPreferences.getLong(QKPreference.LAST_AUTO_DELETE_CHECK));

        Calendar current = Calendar.getInstance();

        // Continue if the auto delete setting is enabled, and we haven't done a purge today
        if (QKPreferences.getBoolean(QKPreference.AUTO_DELETE) && (last.getTimeInMillis() == 0 ||
                current.get(Calendar.DAY_OF_YEAR) != last.get(Calendar.DAY_OF_YEAR) ||
                current.get(Calendar.YEAR) != last.get(Calendar.YEAR))) {
            Log.i(TAG, "Ready to delete old messages");
            QKPreferences.setLong(QKPreference.LAST_AUTO_DELETE_CHECK, System.currentTimeMillis());

            deleteOldUnreadMessages(this);
            deleteOldReadMessages(this);
        } else {
            Log.i(TAG, "Not going to delete old messages");
        }
    }

    private void deleteOldUnreadMessages(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(QKPreferences.getString(QKPreference.AUTO_DELETE_UNREAD)));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        int count = deleteOldMessages(context, SmsHelper.UNREAD_SELECTION, calendar.getTimeInMillis());
        Log.i(TAG, "Deleted unread messages: " + count);
    }

    private void deleteOldReadMessages(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(QKPreferences.getString(QKPreference.AUTO_DELETE_READ)));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        int count = deleteOldMessages(context, SmsHelper.READ_SELECTION + "=" + SmsHelper.READ, calendar.getTimeInMillis());
        Log.i(TAG, "Deleted read messages: " + count);
    }

    private int deleteOldMessages(Context context, String selection, long before) {
        selection += " AND " + SmsHelper.COLUMN_DATE + "<=?";

        try {
            return context.getContentResolver().delete(SmsHelper.SMS_CONTENT_PROVIDER, selection, new String[]{String.valueOf(before)});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void setupAutoDeleteAlarm(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 3); // We want this service to run when the phone is not likely being used

        Intent intent = new Intent(context, DeleteOldMessagesService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 9237, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pIntent);
    }
}
