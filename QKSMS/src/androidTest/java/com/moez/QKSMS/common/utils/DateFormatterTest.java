package com.moez.QKSMS.common.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.text.SimpleDateFormat;

public class DateFormatterTest extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test24Hours() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(SettingsFragment.TIMESTAMPS_24H, true).apply();
        assertTrue(DateFormatter.accountFor24HourTime(getContext(), new SimpleDateFormat("h:mm a")).equals(new SimpleDateFormat("H:mm")));
        prefs.edit().putBoolean(SettingsFragment.TIMESTAMPS_24H, false).apply();
    }

    public void testDateSymbolsAPI(){
        assertEquals(new SimpleDateFormat("H:mm a").toPattern(), "H:mm a");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
