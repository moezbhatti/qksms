package com.moez.QKSMS.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import com.moez.QKSMS.LogTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipientIdCache {
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "Mms/cache";

    private static Uri sAllCanonical =
            Uri.parse("content://mms-sms/canonical-addresses");

    private static Uri sSingleCanonicalAddressUri =
            Uri.parse("content://mms-sms/canonical-address");

    private static RecipientIdCache sInstance;
    static RecipientIdCache getInstance() { return sInstance; }

    private final Map<Long, String> mCache;

    private final Context mContext;

    public static class Entry {
        public long id;
        public String number;

        public Entry(long id, String number) {
            this.id = id;
            this.number = number;
        }
    }

    static void init(Context context) {
        sInstance = new RecipientIdCache(context);
        new Thread(new Runnable() {
            public void run() {
                fill();
            }
        }, "RecipientIdCache.init").start();
    }

    RecipientIdCache(Context context) {
        mCache = new HashMap<Long, String>();
        mContext = context;
    }

    public static void fill() {
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[RecipientIdCache] fill: begin");
        }

        Context context = sInstance.mContext;
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                sAllCanonical, null, null, null, null);
        if (c == null) {
            Log.w(TAG, "null Cursor in fill()");
            return;
        }

        try {
            synchronized (sInstance) {
                // Technically we don't have to clear this because the stupid
                // canonical_addresses table is never GC'ed.
                sInstance.mCache.clear();
                while (c.moveToNext()) {
                    // TODO: don't hardcode the column indices
                    long id = c.getLong(0);
                    String number = c.getString(1);
                    sInstance.mCache.put(id, number);
                }
            }
        } finally {
            c.close();
        }

        if (LogTag.VERBOSE || Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[RecipientIdCache] fill: finished");
            dump();
        }
    }

    public static List<Entry> getAddresses(String spaceSepIds) {
        synchronized (sInstance) {
            List<Entry> numbers = new ArrayList<>();
            String[] ids = spaceSepIds.split(" ");
            for (String id : ids) {
                long longId;

                try {
                    longId = Long.parseLong(id);
                } catch (NumberFormatException ex) {
                    // skip this id
                    continue;
                }

                String number = sInstance.mCache.get(longId);

                if (number == null) {
                    Log.w(TAG, "RecipientId " + longId + " not in cache!");
                    if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                        dump();
                    }

                    fill();
                    number = sInstance.mCache.get(longId);
                }

                if (TextUtils.isEmpty(number)) {
                    Log.w(TAG, "RecipientId " + longId + " has empty number!");
                } else {
                    numbers.add(new Entry(longId, number));
                }
            }
            return numbers;
        }
    }

    public static void updateNumbers(long threadId, ContactList contacts) {
        long recipientId = 0;

        for (Contact contact : contacts) {
            if (contact.isNumberModified()) {
                contact.setIsNumberModified(false);
            } else {
                // if the contact's number wasn't modified, don't bother.
                continue;
            }

            recipientId = contact.getRecipientId();
            if (recipientId == 0) {
                continue;
            }

            String number1 = contact.getNumber();
            boolean needsDbUpdate = false;
            synchronized (sInstance) {
                String number2 = sInstance.mCache.get(recipientId);

                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    Log.d(TAG, "[RecipientIdCache] updateNumbers: contact=" + contact +
                            ", wasModified=true, recipientId=" + recipientId);
                    Log.d(TAG, "   contact.getNumber=" + number1 +
                            ", sInstance.mCache.getConversation(recipientId)=" + number2);
                }

                // if the numbers don't match, let's update the RecipientIdCache's number
                // with the new number in the contact.
                if (!number1.equalsIgnoreCase(number2)) {
                    sInstance.mCache.put(recipientId, number1);
                    needsDbUpdate = true;
                }
            }
            if (needsDbUpdate) {
                // Do this without the lock held.
                sInstance.updateCanonicalAddressInDb(recipientId, number1);
            }
        }
    }

    private void updateCanonicalAddressInDb(long id, String number) {
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "[RecipientIdCache] updateCanonicalAddressInDb: id=" + id +
                    ", number=" + number);
        }

        final ContentValues values = new ContentValues();
        values.put(Telephony.CanonicalAddressesColumns.ADDRESS, number);

        final StringBuilder buf = new StringBuilder(Telephony.CanonicalAddressesColumns._ID);
        buf.append('=').append(id);

        final Uri uri = ContentUris.withAppendedId(sSingleCanonicalAddressUri, id);
        final ContentResolver cr = mContext.getContentResolver();

        // We're running on the UI thread so just fire & forget, hope for the best.
        // (We were ignoring the return value anyway...)
        new Thread("updateCanonicalAddressInDb") {
            public void run() {
                cr.update(uri, values, buf.toString(), null);
            }
        }.start();
    }

    public static void dump() {
        // Only dump user private data if we're in special debug mode
        synchronized (sInstance) {
            Log.d(TAG, "*** Recipient ID cache dump ***");
            for (Long id : sInstance.mCache.keySet()) {
                Log.d(TAG, id + ": " + sInstance.mCache.get(id));
            }
        }
    }

    public static void canonicalTableDump() {
        Log.d(TAG, "**** Dump of canoncial_addresses table ****");
        Context context = sInstance.mContext;
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                sAllCanonical, null, null, null, null);
        if (c == null) {
            Log.w(TAG, "null Cursor in content://mms-sms/canonical-addresses");
        }
        try {
            while (c.moveToNext()) {
                // TODO: don't hardcode the column indices
                long id = c.getLong(0);
                String number = c.getString(1);
                Log.d(TAG, "id: " + id + " number: " + number);
            }
        } finally {
            c.close();
        }
    }

    /**
     * getSingleNumberFromCanonicalAddresses looks up the recipientId in the canonical_addresses
     * table and returns the associated number or email address.
     * @param context needed for the ContentResolver
     * @param recipientId of the contact to look up
     * @return phone number or email address of the recipientId
     */
    public static String getSingleAddressFromCanonicalAddressInDb(final Context context,
                                                                  final String recipientId) {
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                ContentUris.withAppendedId(sSingleCanonicalAddressUri, Long.parseLong(recipientId)),
                null, null, null, null);
        if (c == null) {
            LogTag.warn(TAG, "null Cursor looking up recipient: " + recipientId);
            return null;
        }
        try {
            if (c.moveToFirst()) {
                String number = c.getString(0);
                return number;
            }
        } finally {
            c.close();
        }
        return null;
    }

    // used for unit tests
    public static void insertCanonicalAddressInDb(final Context context, String number) {
        if (LogTag.VERBOSE || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "[RecipientIdCache] insertCanonicalAddressInDb: number=" + number);
        }

        final ContentValues values = new ContentValues();
        values.put(Telephony.CanonicalAddressesColumns.ADDRESS, number);

        final ContentResolver cr = context.getContentResolver();

        // We're running on the UI thread so just fire & forget, hope for the best.
        // (We were ignoring the return value anyway...)
        new Thread("insertCanonicalAddressInDb") {
            public void run() {
                cr.insert(sAllCanonical, values);
            }
        }.start();
    }

}
