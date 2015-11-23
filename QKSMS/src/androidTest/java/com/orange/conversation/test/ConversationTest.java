package com.orange.conversation.test;


import android.test.AndroidTestCase;

import android.content.Context;

import com.moez.QKSMS.data.ContactList;
import com.moez.QKSMS.data.Conversation;



public class ConversationTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetWithContactListNormalExecution() throws Exception {
        Context context = getContext();
        boolean allowQuery = true;
        long threadId = 0;

        // The threadId passed within Cache.get(threadId) is set to null
        // Cache is a private inner class
        Conversation conv = Conversation.get(context, threadId, allowQuery);
        assertEquals(0,conv.getThreadId());

    }


}