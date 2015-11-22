package com.orange.message.test;

import android.test.AndroidTestCase;
import com.moez.QKSMS.mmssms.Message;

public class MessageTest extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCreateMessageSingleAddress(){
        String singleText = "text";
        String singleAddress = "address";

        Message m = new Message(singleText, singleAddress);
        assertEquals(m.getText(), "text");
        assertEquals(m.getAddresses()[0], "address");
    }

    public void testCreateMessageMultipleAddress(){
        String singleText = "text";
        String arrayAddress[] = {"address1", "address2", "address3"};

        Message m = new Message(singleText, arrayAddress);
        assertEquals(m.getText(), "text");
        assertEquals(m.getAddresses(), arrayAddress);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
