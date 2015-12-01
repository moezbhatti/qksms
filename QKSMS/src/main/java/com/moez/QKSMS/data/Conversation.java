package com.moez.QKSMS.data;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.provider.Telephony.Threads;
import android.provider.Telephony.ThreadsColumns;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.transaction.MmsMessageSender;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.moez.QKSMS.QKSMSAppBase;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.LogTag;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.google.DraftCache;
import com.moez.QKSMS.receiver.UnreadBadgeService;
import com.moez.QKSMS.common.utils.AddressUtils;
import com.moez.QKSMS.common.utils.PhoneNumberUtils;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An interface for finding information about conversations and/or creating new ones.
 */
public class Conversation {

    private static final String TAG = "Mms/conv";
    private static final boolean DEBUG = false;
    private static final boolean DELETEDEBUG = false;



    public static final Uri sAllThreadsUri =
            Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();

    public static final String[] ALL_THREADS_PROJECTION = {
            Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
            Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
            Threads.HAS_ATTACHMENT
    };

    public static final String[] UNREAD_PROJECTION = {
            Threads._ID,
            Threads.READ
    };

    private static final String UNREAD_SELECTION = "(read=0 OR seen=0)";

    private static final String[] SEEN_PROJECTION = new String[]{
            "seen"
    };

    public static final int ID = 0;
    public static final int DATE = 1;
    public static final int MESSAGE_COUNT = 2;
    public static final int RECIPIENT_IDS = 3;
    public static final int SNIPPET = 4;
    public static final int SNIPPET_CS = 5;
    public static final int READ = 6;
    public static final int ERROR = 7;
    public static final int HAS_ATTACHMENT = 8;


    private final Context mContext;

    // The thread ID of this conversation.  Can be zero in the case of a
    // new conversation where the recipient set is changing as the user
    // types and we have not hit the database yet to create a thread.
    private long mThreadId;

    private ContactList mRecipients;    // The current set of recipients.
    private long mDate;                 // The last update time.
    private int mMessageCount;          // Number of messages.
    private String mSnippet;            // Text of the most recent message.
    private boolean mHasUnreadMessages; // True if there are unread messages.
    private boolean mHasAttachment;     // True if any message has an attachment.
    private boolean mHasError;          // True if any message is in an error state.
    private boolean mIsChecked;         // True if user has selected the conversation for a
    // multi-operation such as delete.

    private static ContentValues sReadContentValues;
    private static boolean sLoadingThreads;
    private static boolean sDeletingThreads;
    private static Object sDeletingThreadsLock = new Object();
    private boolean mMarkAsReadBlocked;
    private boolean mMarkAsReadWaiting;

    public static boolean issDeletingThreads() {
        return sDeletingThreads;
    }

    public static String getTAG() {
        return TAG;
    }

    public static boolean isDEBUG() {
        return DEBUG;
    }

    public static boolean isDELETEDEBUG() {
        return DELETEDEBUG;
    }

    public static Object getsDeletingThreadsLock() {
        return sDeletingThreadsLock;
    }

    private Conversation(Context context) {
        mContext = context;
        mRecipients = new ContactList();
        mThreadId = 0;
    }

    private Conversation(Context context, long threadId, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor threadId: " + threadId);
        }
        mContext = context;
        if (!loadFromThreadId(threadId, allowQuery)) {
            mRecipients = new ContactList();
            mThreadId = 0;
        }
    }

    private Conversation(Context context, Cursor cursor, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation constructor cursor, allowQuery: " + allowQuery);
        }
        mContext = context;
        fillFromCursor(context, this, cursor, allowQuery);
    }

    /**
     * Create a new conversation with no recipients.  {@link #setRecipients} can
     * be called as many times as you like; the conversation will not be
     * created in the database until {@link #ensureThreadId} is called.
     */
    public static Conversation createNew(Context context) {
        return new Conversation(context);
    }

    /**
     * Find the conversation matching the provided thread ID.
     */
    public static Conversation getConversation(Context context, long threadId, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation getConversation by threadId: " + threadId);
        }
        Conversation conv = Cache.get(threadId);
        if (conv != null)
            return conv;

        conv = new Conversation(context, threadId, allowQuery);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from threadId): " + conv);
            if (!Cache.replace(conv)) {
                LogTag.error("getConversation by threadId cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    /**
     * Find the conversation matching the provided recipient set.
     * When called with an empty recipient list, equivalent to {@link #createNew}.
     */
    public static Conversation getConversation(Context context, ContactList recipients, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation getConversation by recipients: " + recipients.serialize());
        }
        // If there are no recipients in the list, make a new conversation.
        if (recipients.size() < 1) {
            return createNew(context);
        }

        Conversation conv = Cache.get(recipients);
        if (conv != null)
            return conv;

        long threadId = getOrCreateThreadId(context, recipients);
        conv = getConversation(context, threadId, allowQuery);
        conv = new Conversation(context, threadId, allowQuery);
        Log.d(TAG, "Conversation.getConversation: created new conversation " + /*conv.toString()*/ "xxxxxxx");

        if (!conv.getRecipients().equals(recipients)) {
            LogTag.error(TAG, "Conversation.getConversation: new conv's recipients don't match input recpients "
                    + /*recipients*/ "xxxxxxx");
        }

        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from recipients): " + conv);
            if (!Cache.replace(conv)) {
               LogTag.error("getConversation by recipients cache.replace failed on " + conv);
            }
        }

        return conv;
    }

    /**
     * Find the conversation matching in the specified Uri.
     * When called with a null Uri, equivalent to {@link #createNew}.
     */
    public static Conversation getConversation(Context context, Uri uri, boolean allowQuery) {
        if (DEBUG) {
            Log.v(TAG, "Conversation getConversation by uri: " + uri);
        }
        if (uri == null) {
            return createNew(context);
        }

        if (DEBUG) Log.v(TAG, "Conversation getConversation URI: " + uri);

        // Handle a conversation URI
        if (uri.getPathSegments().size() >= 2) {
            try {
                long threadId = Long.parseLong(uri.getPathSegments().get(1));
                if (DEBUG) {
                    Log.v(TAG, "Conversation getConversation threadId: " + threadId);
                }
                return getConversation(context, threadId, allowQuery);
            } catch (NumberFormatException exception) {
                LogTag.error("Invalid URI: " + uri);
            }
        }

        String recipients = PhoneNumberUtils.replaceUnicodeDigits(getRecipients(uri))
                .replace(',', ';');
        return getConversation(context, ContactList.getByNumbers(recipients,
                allowQuery /* don't block */, true /* replace number */), allowQuery);
    }

    /**
     * Returns true if the recipient in the uri matches the recipient list in this
     * conversation.
     */
    public boolean sameRecipient(Uri uri, Context context) {
        int size = mRecipients.size();
        if (size > 1) {
            return false;
        }
        if (uri == null) {
            return size == 0;
        }
        ContactList incomingRecipient = null;
        if (uri.getPathSegments().size() >= 2) {
            // it's a thread id for a conversation
            Conversation otherConv = getConversation(context, uri, false);
            if (otherConv == null) {
                return false;
            }
            incomingRecipient = otherConv.mRecipients;
        } else {
            String recipient = getRecipients(uri);
            incomingRecipient = ContactList.getByNumbers(recipient,
                    false /* don't block */, false /* don't replace number */);
        }
        if (DEBUG) Log.v(TAG, "sameRecipient incomingRecipient: " + incomingRecipient +
                " mRecipients: " + mRecipients);
        return mRecipients.equals(incomingRecipient);
    }

    /**
     * Returns a temporary Conversation (not representing one on disk) wrapping
     * the contents of the provided cursor.  The cursor should be the one
     * returned to your AsyncQueryHandler passed in to {@link #startQueryForAll}.
     * The recipient list of this conversation can be empty if the results
     * were not in cache.
     */
    public static Conversation from(Context context, Cursor cursor) {
        // First look in the cache for the Conversation and return that one. That way, all the
        // people that are looking at the cached copy will getConversation updated when fillFromCursor() is
        // called with this cursor.
        long threadId = cursor.getLong(ID);
        if (threadId > 0) {
            Conversation conv = Cache.get(threadId);
            if (conv != null) {
                fillFromCursor(context, conv, cursor, false);   // update the existing conv in-place
                return conv;
            }
        }
        Conversation conv = new Conversation(context, cursor, false);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error(TAG, "Tried to add duplicate Conversation to Cache (from cursor): " +
                    conv);
            if (!Cache.replace(conv)) {
                LogTag.error("Converations.from cache.replace failed on " + conv);
            }
        }
        return conv;
    }

    private void buildReadContentValues() {
        if (sReadContentValues == null) {
            sReadContentValues = new ContentValues(2);
            sReadContentValues.put("read", 1);
            sReadContentValues.put("seen", 1);
        }
    }

    private void sendReadReport(final Context context,
                                final long threadId,
                                final int status) {
        String selection = Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
                + " AND " + Mms.READ + " = 0"
                + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES;

        if (threadId != -1) {
            selection = selection + " AND " + Mms.THREAD_ID + " = " + threadId;
        }

        final Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                Mms.Inbox.CONTENT_URI, new String[]{Mms._ID, Mms.MESSAGE_ID},
                selection, null, null);

        try {
            if (c == null || c.getCount() == 0) {
                return;
            }

            while (c.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    LogTag.debug("sendReadReport: uri = " + uri);
                }
                MmsMessageSender.sendReadRec(context, AddressUtils.getFrom(context, uri),
                        c.getString(1), status);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    /**
     * Marks all messages in this conversation as read and updates
     * relevant notifications.  This method returns immediately;
     * work is dispatched to a background thread. This function should
     * always be called from the UI thread.
     */
    public void markAsRead() {
        if (DELETEDEBUG) {
            Contact.logWithTrace(TAG, "markAsRead mMarkAsReadWaiting: " + mMarkAsReadWaiting +
                    " mMarkAsReadBlocked: " + mMarkAsReadBlocked);
        }
        if (mMarkAsReadWaiting) {
            // We've already been asked to mark everything as read, but we're blocked.
            return;
        }
        if (mMarkAsReadBlocked) {
            // We're blocked so record the fact that we want to mark the messages as read
            // when we getConversation unblocked.
            mMarkAsReadWaiting = true;
            return;
        }
        final Uri threadUri = getUri();

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... none) {
                if (DELETEDEBUG || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    LogTag.debug("markAsRead.doInBackground");
                }
                // If we have no Uri to mark (as in the case of a conversation that
                // has not yet made its way to disk), there's nothing to do.
                if (threadUri != null) {
                    buildReadContentValues();

                    // Check the read flag first. It's much faster to do a query than
                    // to do an update. Timing this function show it's about 10x faster to
                    // do the query compared to the update, even when there's nothing to
                    // update.
                    boolean needUpdate = true;

                    Cursor c = mContext.getContentResolver().query(threadUri,
                            UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                    if (c != null) {
                        try {
                            needUpdate = c.getCount() > 0;
                        } finally {
                            c.close();
                        }
                    }

                    if (needUpdate) {
                        sendReadReport(mContext, mThreadId, PduHeaders.READ_STATUS_READ);
                        LogTag.debug("markAsRead: update read/seen for thread uri: " +
                                threadUri);
                        mContext.getContentResolver().update(threadUri, sReadContentValues,
                                UNREAD_SELECTION, null);
                    }
                    setHasUnreadMessages(false);
                }
                // Always update notifications regardless of the read state, which is usually
                // canceling the notification of the thread that was just marked read.
                //MessagingNotification.blockingUpdateAllNotifications(mContext, MessagingNotification.THREAD_NONE);
                NotificationManager.update(mContext);

                UnreadBadgeService.update(mContext);

                return null;
            }
        }.execute();
    }

    /**
     * Call this with false to prevent marking messages as read. The code calls this so
     * the DB queries in markAsRead don't slow down the main query for messages. Once we've
     * queried for all the messages (see ComposeMessageActivity.onQueryComplete), then we
     * can mark messages as read. Only call this function on the UI thread.
     */
    public void blockMarkAsRead(boolean block) {
        if (DELETEDEBUG || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("blockMarkAsRead: " + block);
        }

        if (block != mMarkAsReadBlocked) {
            mMarkAsReadBlocked = block;
            if (!mMarkAsReadBlocked) {
                if (mMarkAsReadWaiting) {
                    mMarkAsReadWaiting = false;
                    markAsRead();
                }
            }
        }
    }

    /**
     * Returns a content:// URI referring to this conversation,
     * or null if it does not exist on disk yet.
     */
    public synchronized Uri getUri() {
        if (mThreadId <= 0)
            return null;

        return ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);
    }

    /**
     * Return the Uri for all messages in the given thread ID.
     *
     * @deprecated
     */
    public static Uri getUri(long threadId) {
        // TODO: Callers using this should really just have a Conversation
        // and call getUri() on it, but this guarantees no blocking.
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }

    /**
     * Returns the thread ID of this conversation.  Can be zero if
     * {@link #ensureThreadId} has not been called yet.
     */
    public synchronized long getThreadId() {
        return mThreadId;
    }

    /**
     * Guarantees that the conversation has been created in the database.
     * This will make a blocking database call if it hasn't.
     *
     * @return The thread ID of this conversation in the database
     */
    public synchronized long ensureThreadId() {
        if (DEBUG || DELETEDEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }
        if (mThreadId <= 0) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients);
        }
        if (DEBUG || DELETEDEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }

        return mThreadId;
    }

    public synchronized void clearThreadId() {
        // remove ourself from the cache
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("clearThreadId old threadId was: " + mThreadId + " now zero");
        }
        Cache.remove(mThreadId);

        mThreadId = 0;
    }

    /**
     * Sets the list of recipients associated with this conversation.
     * If called, {@link #ensureThreadId} must be called before the next
     * operation that depends on this conversation existing in the
     * database (e.g. storing a draft message to it).
     */
    public synchronized void setRecipients(ContactList list) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "setRecipients before: " + this.toString());
        }
        mRecipients = list;

        // Invalidate thread ID because the recipient set has changed.
        mThreadId = 0;

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "setRecipients after: " + this.toString());
        }
    }

    /**
     * Returns the recipient set of this conversation.
     */
    public synchronized ContactList getRecipients() {
        return mRecipients;
    }

    /**
     * Returns true if a draft message exists in this conversation.
     */
    public synchronized boolean hasDraft() {
        if (mThreadId <= 0)
            return false;

        return DraftCache.getInstance().hasDraft(mThreadId);
    }

    /**
     * Sets whether or not this conversation has a draft message.
     */
    public synchronized void setDraftState(boolean hasDraft) {
        if (mThreadId <= 0)
            return;

        DraftCache.getInstance().setDraftState(mThreadId, hasDraft);
    }

    /**
     * Returns the time of the last update to this conversation in milliseconds,
     * on the {@link System#currentTimeMillis} timebase.
     */
    public synchronized long getDate() {
        return mDate;
    }

    /**
     * Returns the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized int getMessageCount() {
        return mMessageCount;
    }

    /**
     * Set the number of messages in this conversation, excluding the draft
     * (if it exists).
     */
    public synchronized void setMessageCount(int cnt) {
        mMessageCount = cnt;
    }

    /**
     * Returns a snippet of text from the most recent message in the conversation.
     */
    public synchronized String getSnippet() {
        return mSnippet;
    }

    /**
     * Returns true if there are any unread messages in the conversation.
     */
    public boolean hasUnreadMessages() {
        synchronized (this) {
            return mHasUnreadMessages;
        }
    }

    private void setHasUnreadMessages(boolean flag) {
        synchronized (this) {
            mHasUnreadMessages = flag;
        }
    }

    /**
     * Returns true if any messages in the conversation have attachments.
     */
    public synchronized boolean hasAttachment() {
        return mHasAttachment;
    }

    /**
     * Returns true if any messages in the conversation are in an error state.
     */
    public synchronized boolean hasError() {
        return mHasError;
    }

    /**
     * Returns true if this conversation is selected for a multi-operation.
     */
    public synchronized boolean isChecked() {
        return mIsChecked;
    }

    public synchronized void setIsChecked(boolean isChecked) {
        mIsChecked = isChecked;
    }

    private static long getOrCreateThreadId(Context context, ContactList list) {
        HashSet<String> recipients = new HashSet<>();
        Contact cacheContact = null;
        for (Contact c : list) {
            cacheContact = Contact.get(c.getNumber(), false);
            if (cacheContact != null) {
                recipients.add(cacheContact.getNumber());
            } else {
                recipients.add(c.getNumber());
            }
        }
        synchronized (sDeletingThreadsLock) {
            if (DELETEDEBUG) {
                Log.d(TAG, "Conversation getOrCreateThreadId for: " + list.formatNamesAndNumbers(",") + " sDeletingThreads: " + sDeletingThreads);
            }
            long now = System.currentTimeMillis();
            while (sDeletingThreads) {
                try {
                    sDeletingThreadsLock.wait(30000);
                } catch (InterruptedException e) {
                }
                if (System.currentTimeMillis() - now > 29000) {
                    // The deleting thread task is stuck or onDeleteComplete wasn't called.
                    // Unjam ourselves.
                    Log.e(TAG, "getOrCreateThreadId timed out waiting for delete to complete",
                            new Exception());
                    sDeletingThreads = false;
                    break;
                }
            }
            long retVal = Utils.getOrCreateThreadId(context, recipients);
            if (DELETEDEBUG || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                        recipients, retVal);
            }
            return retVal;
        }
    }

    public static long getOrCreateThreadId(Context context, String address) {
        synchronized (sDeletingThreadsLock) {
            if (DELETEDEBUG) {
                Log.d(TAG, "Conversation getOrCreateThreadId for: " + address + " sDeletingThreads: " + sDeletingThreads);
            }
            long now = System.currentTimeMillis();
            while (sDeletingThreads) {
                try {
                    sDeletingThreadsLock.wait(30000);
                } catch (InterruptedException e) {
                }
                if (System.currentTimeMillis() - now > 29000) {
                    // The deleting thread task is stuck or onDeleteComplete wasn't called.
                    // Unjam ourselves.
                    Log.e(TAG, "getOrCreateThreadId timed out waiting for delete to complete",
                            new Exception());
                    sDeletingThreads = false;
                    break;
                }
            }
            long retVal = Utils.getOrCreateThreadId(context, address);
            if (DELETEDEBUG || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                        address, retVal);
            }
            return retVal;
        }
    }

    /*
     * The primary key of a conversation is its recipient set; override
     * equals() and hashCode() to just pass through to the internal
     * recipient sets.
     */
    @Override
    public synchronized boolean equals(Object obj) {
        try {
            Conversation other = (Conversation) obj;
            return (mRecipients.equals(other.mRecipients));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public synchronized int hashCode() {
        return mRecipients.hashCode();
    }

    @Override
    public synchronized String toString() {
        return String.format("[%s] (tid %d)", mRecipients.serialize(), mThreadId);
    }

    /**
     * Remove any obsolete conversations sitting around on disk. Obsolete threads are threads
     * that aren't referenced by any message in the pdu or sms tables.
     */
    public static void asyncDeleteObsoleteThreads(AsyncQueryHandler handler, int token) {
        handler.startDelete(token, null, Threads.OBSOLETE_THREADS_URI, null, null);
    }

    /**
     * Start a query for all conversations in the database on the specified
     * AsyncQueryHandler.
     *
     * @param handler An AsyncQueryHandler that will receive onQueryComplete
     *                upon completion of the query
     * @param token   The token that will be passed to onQueryComplete
     */
    public static void startQueryForAll(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC

        startQuery(handler, token, null);
    }

    /**
     * Start a query for in the database on the specified AsyncQueryHandler with the specified
     * "where" clause.
     *
     * @param handler   An AsyncQueryHandler that will receive onQueryComplete
     *                  upon completion of the query
     * @param token     The token that will be passed to onQueryComplete
     * @param selection A where clause (can be null) to select particular conv items.
     */
    public static void startQuery(AsyncQueryHandler handler, int token, String selection) {
        handler.cancelOperation(token);

        // This query looks like this in the log:
        // I/Database(  147): elapsedTime4Sql|/data/data/com.android.providers.telephony/databases/
        // mmssms.db|2.253 ms|SELECT _id, date, message_count, recipient_ids, snippet, snippet_cs,
        // read, error, has_attachment FROM threads ORDER BY  date DESC

        handler.startQuery(token, null, sAllThreadsUri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }

    /**
     * Start a delete of the conversation with the specified thread ID.
     *
     * @param handler   An AsyncQueryHandler that will receive onDeleteComplete
     *                  upon completion of the conversation being deleted
     * @param token     The token that will be passed to onDeleteComplete
     * @param deleteAll Delete the whole thread including locked messages
     * @param threadIds Collection of thread IDs of the conversations to be deleted
     */
    public static void startDelete(ConversationQueryHandler handler, int token, boolean deleteAll, Collection<Long> threadIds) {
        synchronized (sDeletingThreadsLock) {
            if (DELETEDEBUG) {
                Log.v(TAG, "Conversation startDelete sDeletingThreads: " + sDeletingThreads);
            }
            if (sDeletingThreads) {
                Log.e(TAG, "startDeleteAll already in the middle of a delete", new Exception());
            }
            //MmsApp.getApplication().getPduLoaderManager().clear();
            sDeletingThreads = true;

            for (long threadId : threadIds) {
                Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
                String selection = deleteAll ? null : "locked=0";

                handler.setDeleteToken(token);
                handler.startDelete(token, new Long(threadId), uri, selection, null);

                DraftCache.getInstance().setDraftState(threadId, false);
            }
        }
    }

    /**
     * Start deleting all conversations in the database.
     *
     * @param handler   An AsyncQueryHandler that will receive onDeleteComplete
     *                  upon completion of all conversations being deleted
     * @param token     The token that will be passed to onDeleteComplete
     * @param deleteAll Delete the whole thread including locked messages
     */
    public static void startDeleteAll(ConversationQueryHandler handler, int token,
                                      boolean deleteAll) {
        synchronized (sDeletingThreadsLock) {
            if (DELETEDEBUG) {
                Log.v(TAG, "Conversation startDeleteAll sDeletingThreads: " +
                        sDeletingThreads);
            }
            if (sDeletingThreads) {
                Log.e(TAG, "startDeleteAll already in the middle of a delete", new Exception());
            }
            sDeletingThreads = true;
            String selection = deleteAll ? null : "locked=0";

            QKSMSAppBase app = QKSMSApp.getApplication();
            //app.getPduLoaderManager().clear();
            //app.getThumbnailManager().clear();

            handler.setDeleteToken(token);
            handler.startDelete(token, new Long(-1), Threads.CONTENT_URI, selection, null);
        }
    }

    /**
     * Check for locked messages in all threads or a specified thread.
     *
     * @param handler   An AsyncQueryHandler that will receive onQueryComplete
     *                  upon completion of looking for locked messages
     * @param threadIds A list of threads to search. null means all threads
     * @param token     The token that will be passed to onQueryComplete
     */
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler,
                                                    Collection<Long> threadIds,
                                                    int token) {
        handler.cancelOperation(token);
        Uri uri = MmsSms.CONTENT_LOCKED_URI;

        String selection = null;
        if (threadIds != null) {
            StringBuilder buf = new StringBuilder();
            int i = 0;

            for (long threadId : threadIds) {
                if (i++ > 0) {
                    buf.append(" OR ");
                }
                // We have to build the selection arg into the selection because deep down in
                // provider, the function buildUnionSubQuery takes selectionArgs, but ignores it.
                buf.append(Mms.THREAD_ID).append("=").append(Long.toString(threadId));
            }
            selection = buf.toString();
        }
        handler.startQuery(token, threadIds, uri,
                ALL_THREADS_PROJECTION, selection, null, Conversations.DEFAULT_SORT_ORDER);
    }

    /**
     * Check for locked messages in all threads or a specified thread.
     *
     * @param handler  An AsyncQueryHandler that will receive onQueryComplete
     *                 upon completion of looking for locked messages
     * @param threadId The threadId of the thread to search. -1 means all threads
     * @param token    The token that will be passed to onQueryComplete
     */
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler,
                                                    long threadId,
                                                    int token) {
        ArrayList<Long> threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList<Long>();
            threadIds.add(threadId);
        }
        startQueryHaveLockedMessages(handler, threadIds, token);
    }

    /**
     * Fill the specified conversation with the values from the specified
     * cursor, possibly setting recipients to empty if value allowQuery
     * is false and the recipient IDs are not in cache.  The cursor should
     * be one made via {@link #startQueryForAll}.
     */
    private static void fillFromCursor(Context context, Conversation conv,
                                       Cursor c, boolean allowQuery) {
        synchronized (conv) {
            conv.mThreadId = c.getLong(ID);
            conv.mDate = c.getLong(DATE);
            conv.mMessageCount = c.getInt(MESSAGE_COUNT);

            // Replace the snippet with a default value if it's empty.
            String snippet = SmsHelper.cleanseMmsSubject(context,
                    SmsHelper.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS));
            if (TextUtils.isEmpty(snippet)) {
                snippet = context.getString(R.string.no_subject_view);
            }
            conv.mSnippet = snippet;

            conv.setHasUnreadMessages(c.getInt(READ) == 0);
            conv.mHasError = (c.getInt(ERROR) != 0);
            conv.mHasAttachment = (c.getInt(HAS_ATTACHMENT) != 0);
        }
        // Fill in as much of the conversation as we can before doing the slow stuff of looking
        // up the contacts associated with this conversation.
        String recipientIds = c.getString(RECIPIENT_IDS);
        ContactList recipients = ContactList.getByIds(recipientIds, allowQuery);
        synchronized (conv) {
            conv.mRecipients = recipients;
        }

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            Log.d(TAG, "fillFromCursor: conv=" + conv + ", recipientIds=" + recipientIds);
        }
    }

    /**
     * Private cache for the use of the various forms of Conversation.getConversation.
     */
    private static class Cache {
        private static Cache sInstance = new Cache();

        static Cache getInstance() {
            return sInstance;
        }

        private final HashSet<Conversation> mCache;

        private Cache() {
            mCache = new HashSet<Conversation>(10);
        }

        /**
         * Return the conversation with the specified thread ID, or
         * null if it's not in cache.
         */
        static Conversation get(long threadId) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation getConversation with threadId: " + threadId);
                }
                for (Conversation c : sInstance.mCache) {
                    if (DEBUG) {
                        LogTag.debug("Conversation getConversation() threadId: " + threadId +
                                " c.getThreadId(): " + c.getThreadId());
                    }
                    if (c.getThreadId() == threadId) {
                        return c;
                    }
                }
            }
            return null;
        }

        /**
         * Return the conversation with the specified recipient
         * list, or null if it's not in cache.
         */
        static Conversation get(ContactList list) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation getConversation with ContactList: " + list);
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getRecipients().equals(list)) {
                        return c;
                    }
                }
            }
            return null;
        }

        /**
         * Put the specified conversation in the cache.  The caller
         * should not place an already-existing conversation in the
         * cache, but rather update it in place.
         */
        static void put(Conversation c) {
            synchronized (sInstance) {
                // We update cache entries in place so people with long-
                // held references getConversation updated.
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    Log.d(TAG, "Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }

                if (sInstance.mCache.contains(c)) {
                    if (DEBUG) {
                        dumpCache();
                    }
                    throw new IllegalStateException("cache already contains " + c +
                            " threadId: " + c.mThreadId);
                }
                sInstance.mCache.add(c);
            }
        }

        /**
         * Replace the specified conversation in the cache. This is used in cases where we
         * lookup a conversation in the cache by threadId, but don't find it. The caller
         * then builds a new conversation (from the cursor) and tries to add it, but gets
         * an exception that the conversation is already in the cache, because the hash
         * is based on the recipients and it's there under a stale threadId. In this function
         * we remove the stale entry and add the new one. Returns true if the operation is
         * successful
         */
        static boolean replace(Conversation c) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }

                if (!sInstance.mCache.contains(c)) {
                    if (DEBUG) {
                        dumpCache();
                    }
                    return false;
                }
                // Here it looks like we're simply removing and then re-adding the same object
                // to the hashset. Because the hashkey is the conversation's recipients, and not
                // the thread id, we'll actually remove the object with the stale threadId and
                // then add the the conversation with updated threadId, both having the same
                // recipients.
                sInstance.mCache.remove(c);
                sInstance.mCache.add(c);
                return true;
            }
        }

        static void remove(long threadId) {
            synchronized (sInstance) {
                if (DEBUG) {
                    LogTag.debug("remove threadid: " + threadId);
                    dumpCache();
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getThreadId() == threadId) {
                        sInstance.mCache.remove(c);
                        return;
                    }
                }
            }
        }

        static void dumpCache() {
            synchronized (sInstance) {
                LogTag.debug("Conversation dumpCache: ");
                for (Conversation c : sInstance.mCache) {
                    LogTag.debug("   conv: " + c.toString() + " hash: " + c.hashCode());
                }
            }
        }

        /**
         * Remove all conversations from the cache that are not in
         * the provided set of thread IDs.
         */
        static void keepOnly(Set<Long> threads) {
            synchronized (sInstance) {
                Iterator<Conversation> iter = sInstance.mCache.iterator();
                while (iter.hasNext()) {
                    Conversation c = iter.next();
                    if (!threads.contains(c.getThreadId())) {
                        iter.remove();
                    }
                }
            }
            if (DEBUG) {
                LogTag.debug("after keepOnly");
                dumpCache();
            }
        }
    }

    /**
     * Set up the conversation cache.  To be called once at application
     * startup time.
     */
    public static void init(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                cacheAllThreads(context);
            }
        }, "Conversation.init");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public static void markAllConversationsAsSeen(final Context context) {
        if (DELETEDEBUG || DEBUG) {
            Contact.logWithTrace(TAG, "Conversation.markAllConversationsAsSeen");
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (DELETEDEBUG) {
                    Log.d(TAG, "Conversation.markAllConversationsAsSeen.run");
                }
                blockingMarkAllSmsMessagesAsSeen(context);
                blockingMarkAllMmsMessagesAsSeen(context);

                // Always update notifications regardless of the read state.
                //MessagingNotification.blockingUpdateAllNotifications(context, MessagingNotification.THREAD_NONE);
            }
        }, "Conversation.markAllConversationsAsSeen");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private static void blockingMarkAllSmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Sms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (DELETEDEBUG || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " SMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Sms.Inbox.CONTENT_URI,
                values,
                "seen=0",
                null);
    }

    private static void blockingMarkAllMmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Mms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        if (DELETEDEBUG || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " MMS msgs as seen");
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Mms.Inbox.CONTENT_URI,
                values,
                "seen=0",
                null);

    }

    /**
     * Are we in the process of loading and caching all the threads?.
     */
    public static boolean loadingThreads() {
        synchronized (Cache.getInstance()) {
            return sLoadingThreads;
        }
    }

    private static void cacheAllThreads(Context context) {
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: begin");
        }
        synchronized (Cache.getInstance()) {
            if (sLoadingThreads) {
                return;
            }
            sLoadingThreads = true;
        }

        // Keep track of what threads are now on disk so we
        // can discard anything removed from the cache.
        HashSet<Long> threadsOnDisk = new HashSet<>();

        // Query for all conversations.
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, null);
        try {
            if (c != null) {
                while (c.moveToNext()) {
                    long threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);

                    // Try to find this thread ID in the cache.
                    Conversation conv;
                    synchronized (Cache.getInstance()) {
                        conv = Cache.get(threadId);
                    }

                    if (conv == null) {
                        // Make a new Conversation and put it in
                        // the cache if necessary.
                        conv = new Conversation(context, c, true);
                        try {
                            synchronized (Cache.getInstance()) {
                                Cache.put(conv);
                            }
                        } catch (IllegalStateException e) {
                            LogTag.error("Tried to add duplicate Conversation to Cache" +
                                    " for threadId: " + threadId + " new conv: " + conv);
                            if (!Cache.replace(conv)) {
                                LogTag.error("cacheAllThreads cache.replace failed on " + conv);
                            }
                        }
                    } else {
                        // Or update in place so people with references
                        // to conversations getConversation updated too.
                        fillFromCursor(context, conv, c, true);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
            synchronized (Cache.getInstance()) {
                sLoadingThreads = false;
            }
        }

        // Purge the cache of threads that no longer exist on disk.
        Cache.keepOnly(threadsOnDisk);

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: finished");
            Cache.dumpCache();
        }
    }

    private boolean loadFromThreadId(long threadId, boolean allowQuery) {
        Cursor c = mContext.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId), null, null);
        try {
            if (c.moveToFirst()) {
                fillFromCursor(mContext, this, c, allowQuery);

                if (threadId != mThreadId) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id!" +
                            " threadId=" + threadId + ", mThreadId=" + mThreadId);
                }
            } else {
                LogTag.error("loadFromThreadId: Can't find thread ID " + threadId);
                return false;
            }
        } finally {
            c.close();
        }
        return true;
    }

    public static String getRecipients(Uri uri) {
        String base = uri.getSchemeSpecificPart();
        int pos = base.indexOf('?');
        return (pos == -1) ? base : base.substring(0, pos);
    }

    public static void dump() {
        Cache.dumpCache();
    }

    public static void dumpThreadsTable(Context context) {
        LogTag.debug("**** Dump of threads table ****");
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, "date ASC");
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                String snippet = SmsHelper.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
                Log.d(TAG, "dumpThreadsTable threadId: " + c.getLong(ID) +
                        " " + ThreadsColumns.DATE + " : " + c.getLong(DATE) +
                        " " + ThreadsColumns.MESSAGE_COUNT + " : " + c.getInt(MESSAGE_COUNT) +
                        " " + ThreadsColumns.SNIPPET + " : " + snippet +
                        " " + ThreadsColumns.READ + " : " + c.getInt(READ) +
                        " " + ThreadsColumns.ERROR + " : " + c.getInt(ERROR) +
                        " " + ThreadsColumns.HAS_ATTACHMENT + " : " + c.getInt(HAS_ATTACHMENT) +
                        " " + ThreadsColumns.RECIPIENT_IDS + " : " + c.getString(RECIPIENT_IDS));

                ContactList recipients = ContactList.getByIds(c.getString(RECIPIENT_IDS), false);
                Log.d(TAG, "----recipients: " + recipients.serialize());
            }
        } finally {
            c.close();
        }
    }

    static final String[] SMS_PROJECTION = new String[]{
            BaseColumns._ID,
            // For SMS
            Sms.THREAD_ID,
            Sms.ADDRESS,
            Sms.BODY,
            Sms.DATE,
            Sms.READ,
            Sms.TYPE,
            Sms.STATUS,
            Sms.LOCKED,
            Sms.ERROR_CODE,
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_ID = 0;
    static final int COLUMN_THREAD_ID = 1;
    static final int COLUMN_SMS_ADDRESS = 2;
    static final int COLUMN_SMS_BODY = 3;
    static final int COLUMN_SMS_DATE = 4;
    static final int COLUMN_SMS_READ = 5;
    static final int COLUMN_SMS_TYPE = 6;
    static final int COLUMN_SMS_STATUS = 7;
    static final int COLUMN_SMS_LOCKED = 8;
    static final int COLUMN_SMS_ERROR_CODE = 9;

    public static void dumpSmsTable(Context context) {
        LogTag.debug("**** Dump of sms table ****");
        Cursor c = context.getContentResolver().query(Sms.CONTENT_URI,
                SMS_PROJECTION, null, null, "_id DESC");
        try {
            // Only dump the latest 20 messages
            c.moveToPosition(-1);
            while (c.moveToNext() && c.getPosition() < 20) {
                String body = c.getString(COLUMN_SMS_BODY);
                LogTag.debug("dumpSmsTable " + BaseColumns._ID + ": " + c.getLong(COLUMN_ID) +
                        " " + Sms.THREAD_ID + " : " + c.getLong(DATE) +
                        " " + Sms.ADDRESS + " : " + c.getString(COLUMN_SMS_ADDRESS) +
                        " " + Sms.BODY + " : " + body.substring(0, Math.min(body.length(), 8)) +
                        " " + Sms.DATE + " : " + c.getLong(COLUMN_SMS_DATE) +
                        " " + Sms.TYPE + " : " + c.getInt(COLUMN_SMS_TYPE));
            }
        } finally {
            c.close();
        }
    }

    /**
     * verifySingleRecipient takes a threadId and a string recipient [phone number or email
     * address]. It uses that threadId to lookup the row in the threads table and grab the
     * recipient ids column. The recipient ids column contains a space-separated list of
     * recipient ids. These ids are keys in the canonical_addresses table. The recipient is
     * compared against what's stored in the mmssms.db, but only if the recipient id list has
     * a single address.
     *
     * @param context      is used for getting a ContentResolver
     * @param threadId     of the thread we're sending to
     * @param recipientStr is a phone number or email address
     * @return the verified number or email of the recipient
     */
    public static String verifySingleRecipient(final Context context,
                                               final long threadId, final String recipientStr) {
        if (threadId <= 0) {
            LogTag.error("verifySingleRecipient threadId is ZERO, recipient: " + recipientStr);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        Cursor c = context.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId), null, null);
        if (c == null) {
            LogTag.error("verifySingleRecipient threadId: " + threadId +
                    " resulted in NULL cursor , recipient: " + recipientStr);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        String address = recipientStr;
        String recipientIds;
        try {
            if (!c.moveToFirst()) {
                LogTag.error("verifySingleRecipient threadId: " + threadId +
                        " can't moveToFirst , recipient: " + recipientStr);
                LogTag.dumpInternalTables(context);
                return recipientStr;
            }
            recipientIds = c.getString(RECIPIENT_IDS);
        } finally {
            c.close();
        }
        String[] ids = recipientIds.split(" ");

        if (ids.length != 1) {
            // We're only verifying the situation where we have a single recipient input against
            // a thread with a single recipient. If the thread has multiple recipients, just
            // assume the input number is correct and return it.
            return recipientStr;
        }

        // Get the actual number from the canonical_addresses table for this recipientId
        address = RecipientIdCache.getSingleAddressFromCanonicalAddressInDb(context, ids[0]);

        if (TextUtils.isEmpty(address)) {
            LogTag.error("verifySingleRecipient threadId: " + threadId +
                    " getSingleNumberFromCanonicalAddresses returned empty number for: " +
                    ids[0] + " recipientIds: " + recipientIds);
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        if (PhoneNumberUtils.compareLoosely(recipientStr, address)) {
            // Bingo, we've got a match. We're returning the input number because of area
            // codes. We could have a number in the canonical_address name of "232-1012" and
            // assume the user's phone's area code is 650. If the user sends a message to
            // "(415) 232-1012", it will loosely match "232-1202". If we returned the value
            // from the table (232-1012), the message would go to the wrong person (to the
            // person in the 650 area code rather than in the 415 area code).
            return recipientStr;
        }

        if (context instanceof Activity) {
            LogTag.warnPossibleRecipientMismatch("verifySingleRecipient for threadId: " +
                    threadId + " original recipient: " + recipientStr +
                    " recipient from DB: " + address, (Activity) context);
        }
        LogTag.dumpInternalTables(context);
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("verifySingleRecipient for threadId: " +
                    threadId + " original recipient: " + recipientStr +
                    " recipient from DB: " + address);
        }
        return address;
    }
}
