package com.orange.message.test;

import android.test.AndroidTestCase;
import com.moez.QKSMS.mmssms.Message;

public class mmssmsMessageTest extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructorDefault() throws Exception{
        Message m = new Message();
        String firstAddress = m.getAddresses()[0];

        assertEquals(m.getText(), "");
        assertEquals(firstAddress, "");
    }

    public void testConstructorSingleAddress() throws Exception{
        String singleText = "text";
        String singleAddress = "address";

        Message m = new Message(singleText, singleAddress);
        String firstAddress = m.getAddresses()[0];

        assertEquals(m.getText(), singleText);
        assertEquals(firstAddress, singleAddress);
    }

    public void testConstructorMultipleAddress() throws Exception{
        String singleText = "text";
        String arrayAddress[] = {"address1", "address2", "address3"};

        Message m = new Message(singleText, arrayAddress);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses(), arrayAddress);
    }

    public void testConstructorSubject() throws Exception{
        String singleText = "text";
        String singleAddress = "address";
        String subject = "subject";

        Message m = new Message(singleText, singleAddress, subject);
        String firstAddress = m.getAddresses()[0];

        assertEquals(m.getText(), singleText);
        assertEquals(firstAddress, singleAddress);
        assertEquals(m.getSubject(), subject);
    }

    protected void tearDown() throws Exception{
        super.tearDown();
    }
}
