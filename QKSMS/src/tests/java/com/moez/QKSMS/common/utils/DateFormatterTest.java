package com.moez.QKSMS.common.utils;

import android.test.AndroidTestCase;

import java.text.SimpleDateFormat;

public class DateFormatterTest extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDateSymbolsAPI(){
        assertEquals(new SimpleDateFormat("H:mm a").toPattern(), "H:mm a");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
