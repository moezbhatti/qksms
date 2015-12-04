package com.orange.message.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.data.Message;



/**
 * Created by Joseph on 2015-11-22.
 * Tests for before refactoring methods
 */
public class dataMessageTest extends AndroidTestCase {


    public void setUp() throws Exception {
        super.setUp();

    }

    public void testGetNameNull() throws Exception {
        Context context = getContext();
        long id = 0;
        Message message = new Message(context, id);
        String name = ContactHelper.getName(context, message.getAddress());
        assertEquals(message.getName(), name);
    }

    public void testGetContactId() throws Exception {
        Context context = getContext();
        long id = 0;
        Message message = new Message(context, id);
        long testId = ContactHelper.getId(context, message.getAddress());
        assertEquals(message.getContactId(), testId);
    }

    public void testGetPhotoBitmap() throws Exception {
        Context context = getContext();
        long id = 0;
        Message message = new Message(context, id);
        Bitmap bitmap = ContactHelper.getBitmap(context, message.getContactId());
        assertEquals(message.getPhotoBitmap(), bitmap);
    }

    public void tearDown() throws Exception {

    }
}