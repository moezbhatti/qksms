package com.moez.QKSMS.ui.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import com.moez.QKSMS.common.utils.DateFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private final String TAG = "TimePickerPreference";

    private Preference mPreference;
    private Preference.OnPreferenceChangeListener mListener;
    private SharedPreferences mPrefs;

    public void setPreference(Preference preference) {
        mPreference = preference;
    }

    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener l) {
        mListener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mPreference == null) {
            Log.w(TAG, "No preference set");
            return null;
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");

        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();

        try {
            Date date = simpleDateFormat.parse(mPrefs.getString(mPreference.getKey(), "6:00"));
            c.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean isUsing24HourTime = mPrefs.getBoolean(SettingsFragment.TIMESTAMPS_24H, DateFormat.is24HourFormat(getActivity()));
        return new TimePickerDialog(getActivity(), this, hour, minute, isUsing24HourTime);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Format the minutes with padded zeros so we don't getConversation stuff like "6:2" instead
        // of 6:02
        String newValue = String.format("%d:%02d", hourOfDay, minute);
        mPrefs.edit().putString(mPreference.getKey(), newValue).apply();
        mPreference.setSummary(DateFormatter.getSummaryTimestamp(getActivity(), newValue));
        mListener.onPreferenceChange(mPreference, newValue);
        SettingsFragment.updateAlarmManager(getActivity(), true);
    }
}