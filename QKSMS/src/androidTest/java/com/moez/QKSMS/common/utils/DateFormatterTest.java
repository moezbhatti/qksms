package com.moez.QKSMS.common.utils;

import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.util.Log;

import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormatterTest extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test24Hours(){
        SharedPreferences prefs = MainActivity.getPrefs(getContext());
        prefs.edit().putBoolean(SettingsFragment.TIMESTAMPS_24H, true).commit();
        assertTrue(DateFormatter.accountFor24HourTime(getContext(), new SimpleDateFormat("h:mm a")).equals(new SimpleDateFormat("H:mm")));
        prefs.edit().putBoolean(SettingsFragment.TIMESTAMPS_24H, false).commit();
    }

    public void testFlippedMonthDay(){
        Locale.setDefault(Locale.UK);
        Log.d("TestFlippedMonthDay", DateFormatter.accountForFlippedDayMonth(new SimpleDateFormat("MMM d, h:mm a")).toPattern());
        assertTrue(DateFormatter.accountForFlippedDayMonth(new SimpleDateFormat("MMM d, h:mm a")).equals(new SimpleDateFormat("d MMM, h:mm a")));
        Locale.setDefault(Locale.US);
        assertTrue(DateFormatter.accountForFlippedDayMonth(new SimpleDateFormat("MMM d, h:mm a")).equals(new SimpleDateFormat("MMM d, h:mm a")));
    }

    public void testDateSymbolsAPI(){
        assertEquals(new SimpleDateFormat("H:mm a").toPattern(), "H:mm a");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
