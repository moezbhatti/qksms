package com.orange.message.test;

import android.app.Activity;
import android.content.Context;
import android.test.AndroidTestCase;
import android.widget.TextView;

import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.data.Message;

/**
 * Created by Joseph on 2015-11-22.
 */
public class dataMessageTest extends AndroidTestCase {


    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testGetNameNull() throws Exception {
        Context context = getContext();
        long id = 2;
        Message message = new Message(context, id);
        String name = ContactHelper.getName(context, message.getAddress());
        assertEquals(message.getName(), name);
    }

    public void testGetContactId() throws Exception {

    }

    public void testGetPhotoBitmap() throws Exception {

    }
}