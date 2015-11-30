package com.orange.conversation.test;


import android.test.AndroidTestCase;

import android.content.Context;

import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.ContactList;
import com.moez.QKSMS.data.Conversation;



public class ConversationTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetWithThreadIdNormalExecution() throws Exception {
        Context context = getContext();
        boolean allowQuery = true;
        long threadId = 0;


        // The threadId passed within Cache.getConversation(threadId) is set to null
        // Cache is a private inner class
        Conversation conv = Conversation.getConversation(context, threadId, allowQuery);
        assertEquals(0,conv.getThreadId());

    }


    public void testGetWithContactListNormalExecution() throws Exception {
        Context context = getContext();

        boolean allowQuery = true;
        long threadId = 0;
        ContactList testList  = new ContactList();


        testList.add(Contact.get("tet", true));

        // The threadId passed within Cache.getConversation(threadId) is set to null
        // Cache is a private inner class
        Conversation conv = Conversation.getConversation(context, testList, allowQuery);
        assertEquals(0,conv.getThreadId());
    }
}