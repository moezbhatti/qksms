package com.moez.QKSMS.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.util.Log;

/**
 * Created by IhceneCh on 2015-11-29.
 */
public class ConversationQueryHandler extends AsyncQueryHandler {
    private int mDeleteToken;

    public ConversationQueryHandler(ContentResolver cr) {
        super(cr);
    }

    public int getmDeleteToken() { return mDeleteToken; }
    public void setDeleteToken(int token) {
        mDeleteToken = token;
    }

    /**
     * Always call this super method from your overridden onDeleteComplete function.
     */
    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        if (token == mDeleteToken) {
            // release lock
            synchronized (Conversation.getsDeletingThreadsLock()) {
                boolean sDeletingThreads = Conversation.issDeletingThreads();
                sDeletingThreads = false;
                if (Conversation.isDELETEDEBUG()) {
                    Log.v(Conversation.getTAG(), "Conversation onDeleteComplete sDeletingThreads: " + sDeletingThreads);
                }
                Conversation.getsDeletingThreadsLock().notifyAll();
            }
        }
    }
}
