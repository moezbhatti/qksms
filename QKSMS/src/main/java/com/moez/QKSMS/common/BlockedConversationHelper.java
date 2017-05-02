package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.util.Log;
import android.view.MenuItem;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.PhoneNumberUtils;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * A set of helper methods to group the logic related to blocked conversation
 */
public class BlockedConversationHelper {

    private static final String TAG = "QKSMS::Blocker";

    public static boolean isConversationBlocked(SharedPreferences prefs, long threadId) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        return idStrings.contains(String.valueOf(threadId));
    }

    public static void blockConversation(SharedPreferences prefs, long threadId) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        idStrings.add(String.valueOf(threadId));
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_SENDERS, idStrings).apply();
    }

    public static void unblockConversation(SharedPreferences prefs, long threadId) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        idStrings.remove(String.valueOf(threadId));
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_SENDERS, idStrings).apply();
    }

    public static Set<Long> getBlockedConversationIds(SharedPreferences prefs) {
        Set<String> conversations = getBlockedConversations(prefs);
        Set<Long> ids = new HashSet<>();
        for (String id : conversations) {
            ids.add(Long.parseLong(id));
        }
        return ids;
    }

    public static Set<String> getBlockedConversations(SharedPreferences prefs) {
        return prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
    }

    public static boolean blockFutureConversation(SharedPreferences prefs, String address) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_FUTURE, new HashSet<String>());
        final boolean modified = idStrings.add(address);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_FUTURE, idStrings).apply();
        return modified;
    }

    public static boolean unblockFutureConversation(SharedPreferences prefs, String address) {
        Set<String> idStrings2 = prefs.getStringSet(SettingsFragment.BLOCKED_FUTURE, new HashSet<String>());
        final boolean modified = idStrings2.remove(address);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_FUTURE, idStrings2).apply();
        return modified;
    }

    public static Set<String> getFutureBlockedConversations(SharedPreferences prefs) {
        return prefs.getStringSet(SettingsFragment.BLOCKED_FUTURE, new HashSet<String>());
    }

    public static boolean isFutureBlocked(SharedPreferences prefs, String address) {
        for (String s : getFutureBlockedConversations(prefs)) {
            if (PhoneNumberUtils.compareLoosely(s, address)) {
                return true;
            }
        }

        return false;
    }

    public static String[] getBlockedConversationArray(SharedPreferences prefs) {
        Set<String> idStrings = getBlockedConversations(prefs);
        return idStrings.toArray(new String[idStrings.size()]);
    }

    public static String getCursorSelection(SharedPreferences prefs, boolean blocked) {
        StringBuilder selection = new StringBuilder();
        selection.append(Telephony.Threads.MESSAGE_COUNT);
        selection.append(" != 0");
        selection.append(" AND ");
        selection.append(Telephony.Threads._ID);
        if (!blocked) selection.append(" NOT");
        selection.append(" IN (");

        Set<String> idStrings = getBlockedConversations(prefs);
        for (int i = 0; i < idStrings.size(); i++) {
            selection.append("?");
            if (i < idStrings.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        return selection.toString();
    }

    /**
     * If the user has message blocking enabled, then in the menu of the conversation list, there's an item that says
     * Blocked (#). This method will find the number of blocked unread messages to show in that menu item and bind it
     */
    public static void bindBlockedMenuItem(final Context context, final SharedPreferences prefs, final MenuItem item, boolean showBlocked) {
        if (item == null) {
            return;
        }

        new BindMenuItemTask(context, prefs, item, showBlocked).execute((Void[]) null);
    }

    private static class BindMenuItemTask extends AsyncTask<Void, Void, Integer> {

        private Context mContext;
        private SharedPreferences mPrefs;
        private MenuItem mMenuItem;
        private boolean mShowBlocked;

        private BindMenuItemTask(Context context, SharedPreferences prefs, MenuItem item, boolean showBlocked) {
            mContext = context;
            mPrefs = prefs;
            mMenuItem = item;
            mShowBlocked = showBlocked;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMenuItem.setVisible(mPrefs.getBoolean(SettingsFragment.BLOCK_ENABLED, false));
            mMenuItem.setTitle(mContext.getString(mShowBlocked ? R.string.menu_messages : R.string.menu_blocked));
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int unreadCount = 0;

            // Create a cursor for the conversation list
            Cursor conversationCursor = mContext.getContentResolver().query(
                    SmsHelper.CONVERSATIONS_CONTENT_PROVIDER, Conversation.ALL_THREADS_PROJECTION,
                    getCursorSelection(mPrefs, !mShowBlocked), getBlockedConversationArray(mPrefs), SmsHelper.sortDateDesc);

            if (conversationCursor.moveToFirst()) {
                do {
                    Uri threadUri = Uri.withAppendedPath(Message.MMS_SMS_CONTENT_PROVIDER, conversationCursor.getString(Conversation.ID));
                    Cursor messageCursor = mContext.getContentResolver().query(threadUri, MessageColumns.PROJECTION,
                            SmsHelper.UNREAD_SELECTION, null, SmsHelper.sortDateDesc);
                    unreadCount += messageCursor.getCount();
                    messageCursor.close();
                } while (conversationCursor.moveToNext());
            }

            conversationCursor.close();
            return unreadCount;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Log.d("BindMenuItemTask", "onPostExecute: " + integer);
            mMenuItem.setTitle(mContext.getString(mShowBlocked ? R.string.menu_unblocked_conversations : R.string.menu_blocked_conversations, integer));
        }
    }

    public static class FutureBlockedConversationObservable extends Observable {
        private static FutureBlockedConversationObservable sInstance = new FutureBlockedConversationObservable();

        public static FutureBlockedConversationObservable getInstance() {
            return sInstance;
        }

        public void futureBlockedConversationReceived() {
            synchronized (this) {
                setChanged();
                notifyObservers();
            }
        }
    }


    // _________________________________________________________________________

    /**
     * Check to see if a message from a sender should be blocked or not.
     *
     */
    public static boolean isBlocked(final SharedPreferences prefs,
                                    final Context context,
                                    final Message message,
                                    final String address,
                                    String body,
                                    final long date) {

        if(!prefs.getBoolean(SettingsFragment.BLOCK_ENABLED, false))
            return false;

        if (prefs.getBoolean(SettingsFragment.BLOCK_SKIP_CONTACTS, true) &&
                Contact.get(message.getAddress(), true).existsInDatabase()) {
            Log.d(TAG, "skipping spam check from contact: " + address);
            return false;
        }

        if(address != null && !address.isEmpty()) {
            if (isFutureBlocked(prefs, address)) {
                Log.i(TAG, "blocked from <" + address + "> because of address blacklist");
                return true;
            }

            final String numberPrefix0 = getBlockedNumberPrefixOf(prefs, address);
            if(numberPrefix0 != null) {
                Log.i(TAG, "blocked from <" + address + "> because of number prefix: " + numberPrefix0);
                return true;
            }

            String rippedAddr = "";
            for(int i = 0; i < address.length(); i++) {
                final Character c = address.charAt(i);
                if('0' <= c && c <= '9')
                    rippedAddr += c;
            }

            final String numberPrefix1 = getBlockedNumberPrefixOf(prefs, rippedAddr);
            if(numberPrefix1 != null) {
                Log.i(TAG, "blocked from <" + address + "> because of number prefix: " + numberPrefix1);
                return true;
            }
        }

        if(body != null && !body.isEmpty()) {
            body = preProcessForWord(body);

            final String word = getBlockedWordOf(prefs, body);
            if(word != null) {
                Log.i(TAG, "blocked from <" + address + "> because of spam words: " + word);
                return true;
            }

            final String pattern = getBlockedPatternOf(prefs, body);
            if(pattern != null) {
                Log.i(TAG, "blocked from <" + address + "> because of spam pattern: " + pattern);
                return true;
            }
        }

        return false;
    }

    // ------------------- BLOCK BY WORD BLACKLIST

    /**
     * Whether if {@link String#trim()} should be called on every blacklisted word.
     *
     * See {@link #preProcessForWord(String)}
     */
    private static final boolean TRIM_WORD = false;

    /**
     * Whether if checking against blacklisted words is case-sensitive or not.
     *
     * See {@link #preProcessForWord(String)}
     */
    private static final boolean CASE_SENSITIVE_WORD = false;

    /**
     * Add a value to the collection returned by {@link #getBlockedWords(SharedPreferences)}.
     *
     * @param prefs app shared pref.
     * @return true if the value did not exist in the collection before, and hence the collection was modified.
     * @see #getBlockedWords(SharedPreferences)
     */
    public static boolean blockWord(SharedPreferences prefs, String value) {

        value = preProcessForWord(value);

        final String key = SettingsFragment.BLOCKED_WORD;
        final Set<String> values = prefs.getStringSet(key, new HashSet<String>(0));
        final boolean modified = values.add(value);
        prefs.edit().putStringSet(key, values).apply();

        return modified;
    }

    /**
     * Remove a value from the collection returned by {@link #getBlockedWords(SharedPreferences)}.
     *
     * @param prefs app shared pref.
     * @param value the value to remove.
     * @return true if the value actually existed in the collection, and hence the collection was modified.
     * @see #getBlockedWords(SharedPreferences)
     */
    public static boolean unblockWord(SharedPreferences prefs, String value) {

        value = preProcessForWord(value);

        final String key = SettingsFragment.BLOCKED_WORD;
        final Set<String> values = prefs.getStringSet(key, new HashSet<String>(0));
        final boolean modified = values.remove(value);
        prefs.edit().putStringSet(key, values).apply();

        return modified;
    }

    /**
     * List of all blacklisted words (spam words). If a message contains any of
     * these words, it is considered to be spam and should be blocked.
     *
     * @param prefs app shared pref.
     * @return collection of all the words considered to be spam if seen in a message.
     */
    public static Collection<String> getBlockedWords(SharedPreferences prefs) {

        final String key = SettingsFragment.BLOCKED_WORD;
        return prefs.getStringSet(key, Collections.emptySet());
    }

    /**
     * Returns the first seen spam word in the {@code text}, null if none was found.
     *
     * @param prefs app shared pref.
     * @param value body of the the message to check.
     * @return the first seen spam word in text or null of none was found.
     * @see #getBlockedWords(SharedPreferences)
     */
    private static String getBlockedWordOf(SharedPreferences prefs, final String value) {

        for (final String each : getBlockedWords(prefs))
            if (value.contains(each))
                return each;

        return null;
    }

    private static String preProcessForWord(String value) {

        if(TRIM_WORD)
            value = value.trim();

        if(!CASE_SENSITIVE_WORD)
            value = value.toLowerCase();

        return value;
    }

    // -------------------------- BLOCK BY PATTERN

    /**
     * Add a value to the collection returned by {@link #getBlockedPatterns(SharedPreferences)}.
     *
     * @param prefs app shared pref.
     * @return true if the value did not exist in the collection before, and hence the collection was modified.
     * @see #getBlockedPatterns(SharedPreferences)
     */
    public static boolean blockPattern(SharedPreferences prefs, final String value) {

        if(compilePattern(value) == null)
            return false;

        final String key = SettingsFragment.BLOCKED_PATTERN;
        final Set<String> values = prefs.getStringSet(key, new HashSet<String>(0));
        final boolean modified = values.add(value);
        prefs.edit().putStringSet(key, values).apply();

        return modified;
    }

    /**
     * Remove a value from the collection returned by {@link #getBlockedPatterns(SharedPreferences)}.
     *
     * @param prefs app shared pref.
     * @param value the value to remove.
     * @return true if the value actually existed in the collection, and hence the collection was modified.
     * @see #getBlockedPatterns(SharedPreferences)
     */
    public static boolean unblockPattern(SharedPreferences prefs, final String value) {

        final String key = SettingsFragment.BLOCKED_PATTERN;
        final Set<String> values = prefs.getStringSet(key, new HashSet<String>(0));
        final boolean modified = values.remove(value);
        prefs.edit().putStringSet(key, values).apply();

        return modified;
    }

    /**
     * Collection of all patterns considered to be spam. If a message matches
     * against any of these patterns, then the message is spam and should be
     * blocked.
     *
     * @param prefs app shared pref
     * @return Collection of all patterns considered to be spam.
     */
    public static Collection<String> getBlockedPatterns(SharedPreferences prefs) {

        final String key = SettingsFragment.BLOCKED_PATTERN;
        return prefs.getStringSet(key, Collections.emptySet());
    }

    /**
     * Returns the first spam pattern {@code text} matches against, null if none
     * was found.
     *
     * @param prefs app shared pref.
     * @param value body of the the message to check.
     * @return the first spam pattern text matches against or null if none was found.
     * @see #getBlockedPatterns(SharedPreferences)
     */
    private static String getBlockedPatternOf(SharedPreferences prefs, final String value) {

        Pattern regex;
        for (final String each : getBlockedPatterns(prefs))
            if ((regex = compilePattern(each)) != null)
                if (regex.matcher(value).matches())
                    return each;

        return null;
    }

    public static Pattern compilePattern(final String pattern) {

        if(pattern == null || pattern.isEmpty()) {
            Log.e(TAG, "empty pattern");
            return null;
        }

        try {
            return Pattern.compile(pattern.trim());
        }
        catch (final Exception e) {
            Log.e(TAG, "invalid pattern: " + pattern);
            return null;
        }
    }

    // -------------------- BLOCK BY NUMBER PREFIX

    /**
     * Add a value to the collection returned by {@link #getBlockedNumberPrefixes(SharedPreferences)}
     *
     * @param prefs app shared pref.
     * @return true if the value did not exist in the collection before, and hence the collection was modified.
     * @see #getBlockedNumberPrefixes(SharedPreferences)
     */
    public static boolean blockNumberPrefix(SharedPreferences prefs, final String value) {

        final String key = SettingsFragment.BLOCKED_NUMBER_PREFIX;
        final Set<String> values = prefs.getStringSet(key, new HashSet<String>(0));
        final boolean modified = values.add(value);
        prefs.edit().putStringSet(key, values).apply();

        return modified;
    }

    /**
     * Remove a value from the collection returned by {@link #getBlockedNumberPrefixes(SharedPreferences)}.
     *
     * @param prefs app shared pref.
     * @param value the value to remove.
     * @return true if the value actually existed in the collection, and hence the collection was modified.
     * @see #getBlockedNumberPrefixes(SharedPreferences)
     */
    public static boolean unblockNumberPrefix(SharedPreferences prefs, final String value) {

        final String key = SettingsFragment.BLOCKED_NUMBER_PREFIX;
        final Set<String> values = prefs.getStringSet(key, new HashSet<String>(0));
        final boolean modified = values.remove(value);
        prefs.edit().putStringSet(key, values).apply();

        return modified;
    }

    /**
     * Returns collection of all the number prefixes considered to be spam. If a
     * messages arrives from a number, and that numbers prefix is in this list,
     * that message is concidered to be spam and should be blocked.
     *
     * @param prefs app shared pref.
     * @return collection of all the number prefixes considered to be spam.
     */
    public static Collection<String> getBlockedNumberPrefixes(SharedPreferences prefs) {

        final String key = SettingsFragment.BLOCKED_NUMBER_PREFIX;
        return prefs.getStringSet(key, Collections.emptySet());
    }

    /**
     * Returns the first number prefix {@code text} matches against, null if none
     * was found.
     *
     * @param prefs app shared pref.
     * @param value body of the the message to check.
     * @return the first spam pattern text matches against or null if none was found.
     * @see #getBlockedPatterns(SharedPreferences)
     */
    private static String getBlockedNumberPrefixOf(SharedPreferences prefs, final String value) {

        for (final String each : getBlockedNumberPrefixes(prefs))
            if (value.startsWith(each))
                return each;

        return null;
    }

}

