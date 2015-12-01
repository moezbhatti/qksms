/*package com.orange.message.test;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;
import com.moez.QKSMS.mmssms.Message;

public class mmssmsMessageTest extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testConstructorDefault() throws Exception{
        Message m = new Message();

        assertEquals(m.getText(), "");
        assertEquals(m.getAddresses()[0], "");
    }

    public void testConstructorSingleAddress() throws Exception{
        String singleText = "text";
        String singleAddress = "address";

        Message m = new Message(singleText, singleAddress);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], singleAddress);
    }

    public void testConstructorMutipleAddressSingleString() throws Exception{
        String singleText = "text";
        String singleAddress = "address1 address2 address3";
        String addresses[] = singleAddress.split(" ");

        Message m = new Message(singleText, singleAddress);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], addresses[0]);
        assertEquals(m.getAddresses()[1], addresses[1]);
        assertEquals(m.getAddresses()[2], addresses[2]);
    }

    public void testConstructorMultipleAddress() throws Exception{
        String singleText = "text";
        String addresses[] = {"address1", "address2", "address3"};

        Message m = new Message(singleText, addresses);

        assertEquals(m.getText(), singleText);
        assertSame(m.getAddresses(), addresses);
        assertSame(m.getImages().length, 0);
        assertNull(m.getSubject());
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    public void testConstructorSubject() throws Exception{
        String singleText = "text";
        String singleAddress = "address";
        String subject = "subject";

        Message m = new Message(singleText, singleAddress, subject);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], singleAddress);
        assertEquals(m.getSubject(), subject);
    }

    public void testConstructorImage() throws Exception{
        String singleText = "text";
        String singleAddress = "address";
        int width = 100;
        int height = 100;

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Message m = new Message(singleText, singleAddress, image);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], singleAddress);
        assertEquals(m.getImages()[0], image);
    }

    public void testConstructorImageAndSubject() throws Exception{
        String singleText = "text";
        String singleAddress = "address";
        String subject = "subject";
        int width = 100;
        int height = 100;

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Message m = new Message(singleText, singleAddress, image, subject);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], singleAddress);
        assertSame(m.getImages()[0], image);
        assertEquals(m.getSubject(), subject);
    }

    public void testConstructorMultipleAddressSingleImageNoSubject() throws Exception{
        String singleText = "text";
        String addresses[] = {"address1", "address2", "address3"};
        int width = 100;
        int height = 100;
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Message m = new Message(singleText, addresses, image);

        assertEquals(m.getText(), singleText);
        assertSame(m.getAddresses(), addresses);
        assertSame(m.getImages()[0], image);
        assertNull(m.getSubject());
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    public void testConstructorMultipleAddressSingleImageWithSubject() throws Exception{
        String singleText = "text";
        String addresses[] = {"address1", "address2", "address3"};
        int width = 100;
        int height = 100;
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        String subject = "subject";

        Message m = new Message(singleText, addresses, image, subject);

        assertEquals(m.getText(), singleText);
        assertSame(m.getAddresses(), addresses);
        assertSame(m.getImages()[0], image);
        assertEquals(m.getSubject(), subject);
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    public void testConstructorSingleAddressImageArrayNoSubject() throws Exception{
        String singleText = "text";
        String singleAddress = "address";
        int w1 = 100;
        int h1 = 100;
        int w2 = 200;
        int h2 = 200;
        int w3 = 300;
        int h3 = 300;
        Bitmap images[] = {
                Bitmap.createBitmap(w1, h1, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w3, h3, Bitmap.Config.ARGB_8888)
        };

        Message m = new Message(singleText, singleAddress, images);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], singleAddress);
        assertSame(m.getImages(), images);
        assertNull(m.getSubject());
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    public void testConstructorSingleAddressImageArrayWithSubject() throws Exception{
        String singleText = "text";
        String singleAddress = "address";
        int w1 = 100;
        int h1 = 100;
        int w2 = 200;
        int h2 = 200;
        int w3 = 300;
        int h3 = 300;
        Bitmap images[] = {
                Bitmap.createBitmap(w1, h1, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w3, h3, Bitmap.Config.ARGB_8888)
        };
        String subject = "subject";

        Message m = new Message(singleText, singleAddress, images, subject);

        assertEquals(m.getText(), singleText);
        assertEquals(m.getAddresses()[0], singleAddress);
        assertSame(m.getImages(), images);
        assertEquals(m.getSubject(), subject);
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    public void testConstructorCompleteMessageWithArrays() throws Exception{
        String singleText = "text";
        String addresses[] = {"address1", "address2", "address3"};
        int w1 = 100;
        int h1 = 100;
        int w2 = 200;
        int h2 = 200;
        int w3 = 300;
        int h3 = 300;
        Bitmap images[] = {
                Bitmap.createBitmap(w1, h1, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w3, h3, Bitmap.Config.ARGB_8888)
        };
        String subject = "subject";

        Message m = new Message(singleText, addresses, images, subject);

        assertEquals(m.getText(), singleText);
        assertSame(m.getAddresses(), addresses);
        assertSame(m.getImages(), images);
        assertEquals(m.getSubject(), subject);
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    public void testConstructorCompleteMessageWithArraysNoSubject() throws Exception{
        String singleText = "text";
        String addresses[] = {"address1", "address2", "address3"};
        int w1 = 100;
        int h1 = 100;
        int w2 = 200;
        int h2 = 200;
        int w3 = 300;
        int h3 = 300;
        Bitmap images[] = {
                Bitmap.createBitmap(w1, h1, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(w3, h3, Bitmap.Config.ARGB_8888)
        };

        Message m = new Message(singleText, addresses, images);

        assertEquals(m.getText(), singleText);
        assertSame(m.getAddresses(), addresses);
        assertSame(m.getImages(), images);
        assertNull(m.getSubject());
        assertEquals(m.getMedia().length, 0);
        assertNull(m.getMediaMimeType());
        assertTrue(m.getSave());
        assertEquals(m.getType(), Message.TYPE_SMSMMS);
        assertEquals(m.getDelay(), 0);
    }

    protected void tearDown() throws Exception{
        super.tearDown();
    }
}
*/