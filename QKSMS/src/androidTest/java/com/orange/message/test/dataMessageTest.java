package com.orange.message.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.Message;



/**
 * Created by Joseph on 2015-11-22.
 * Tests for before refactoring methods
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
        String name = Contact.getName(context, message.getAddress());
        assertEquals(message.getName(), name);
    }

    public void testGetContactId() throws Exception {
        Context context = getContext();
        Message message = new Message(context, 0);
        long testId = Contact.getId(context, message.getAddress());
        assertEquals(0, testId);
    }

    public void testGetPhotoBitmap() throws Exception {
        Context context = getContext();
        Message message = new Message(context, 0);
        Bitmap bitmap = Contact.getBitmap(context, message.getContactId());
        assertEquals(null, bitmap);
    }
}