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
            mMenuItem.setVisible(mPrefs.getBoolean(SettingsFragment.BLOCKED_ENABLED, false));
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
     * @param prefs app shared pref.
     * @param address where message is coming from.
     * @param body message text.
     * @return true if message should be blocked, false otherwise.
     */
    public static boolean isBlocked(SharedPreferences prefs, String address, String body) {

        if(!prefs.getBoolean(SettingsFragment.BLOCKED_ENABLED, false))
            return false;

        final boolean skipContacts = prefs.getBoolean(SettingsFragment.BLOCK_SKIP_CONTACTS, true);
        if (skipContacts && Contact.get(address, true).existsInDatabase()) {
            Log.d(TAG, "skipping spam check from contact: " + address);
            return false;
        }

        if (isFutureBlocked(prefs, address)) {
            Log.i(TAG, "blocked from <" + address + "> because of address blacklist");
            return true;
        }

        body = preProcessMessageText(body);

        final String blockedWordOf = getBlockedWordOf(prefs, body);
        if(blockedWordOf != null) {
            Log.i(TAG, "blocked from <" + address + "> because of spam words: " + blockedWordOf);
            return true;
        }

        final String blockedPatternOf = getBlockedPatternOf(prefs, body);
        if(blockedPatternOf != null) {
            Log.i(TAG, "blocked from <" + address + "> because of spam pattern: " + blockedPatternOf);
            return true;
        }

        return false;
    }

    /**
     * if any pre-processing is needed on the message text before
     * checking against spam patterns and spam words.
     *
     * @param text the text to pre-process.
     * @return pre-processed text.
     */
    private static String preProcessMessageText(final String text) {

        return text == null ? null : text.toLowerCase().trim();
    }

    // --------------------------------- WORD BLACKLIST

    /**
     * Whether if {@link String#trim()} should be called on every blacklisted word.
     *
     * See {@link #preProcessWord(String)}
     */
    private static final boolean TRIM_WORD = false;

    /**
     * Whether if {@link String#trim()} should be called on every blacklisted word.
     *
     * See {@link #preProcessWord(String)}
     */
    private static final boolean CASE_SENSITIVE = false;

    /**
     * Add a word to the list of spam words.
     *
     * If later a message contains this word, the message is considered to be
     * spam and should be blocked.
     *
     * </br>
     *
     * If the pattern is invalid, or the pattern already existed in the
     * blacklist, this method return {@code false}.
     *
     *
     * @param prefs app shared pref.
     * @return true if the word did not exist in the blacklist before.
     */
    public static boolean blockWord(SharedPreferences prefs, String word) {

        final Set<String> words = prefs.getStringSet(
                SettingsFragment.BLOCKED_WORD, new HashSet<String>(0));

        final boolean modified = words.add(preProcessWord(word));

        prefs.edit().putStringSet(SettingsFragment.BLOCKED_WORD, words).apply();

        return modified;
    }

    /**
     * Remove a word previously added to list of spam words.
     *
     * @param prefs app shared pref.
     * @return true if the word was actually blacklisted before.
     */
    public static boolean unblockWord(SharedPreferences prefs, String word) {

        final Set<String> words = prefs.getStringSet(
                SettingsFragment.BLOCKED_WORD, new HashSet<String>(0));

        final boolean modified = words.remove(preProcessWord(word));

        prefs.edit().putStringSet(SettingsFragment.BLOCKED_WORD, words).apply();

        return modified;
    }

    /**
     * Returns collection of all the words considered to be spam if seen in a
     * message.
     *
     * @param prefs app shared pref.
     * @return collection of all the words considered to be spam if seen in a message.
     */
    public static Collection<String> getBlockedWords(SharedPreferences prefs) {

        return prefs.getStringSet(SettingsFragment.BLOCKED_WORD, Collections.emptySet());
    }

    /**
     * Returns the first seen spam word in the {@code body}. null if none was found.
     *
     * @param prefs app shared pref.
     * @param body body of the the message to check.
     * @return the first seen spam word in body or null of none was found.
     */
    private static String getBlockedWordOf(SharedPreferences prefs, String body) {

        for (final String word : getBlockedWords(prefs)) {
            if(body.contains(word)) {
                return word;
            }
        }

        return null;
    }

    private static String preProcessWord(String word) {

        if(TRIM_WORD)
            word = word.trim();

        if(!CASE_SENSITIVE)
            word = word.toLowerCase();

        return word;
    }

    // --------------------------------- PATTERN

    public static boolean isValidPattern(final String pattern) {

        if(pattern == null || pattern.isEmpty()) {
            return false;
        }
        try {
            @SuppressWarnings("unused")
            final Pattern test = Pattern.compile(pattern.trim());
        }
        catch (final Exception e) {
            Log.e(TAG, "invalid pattern, skipping: " + pattern);
            return false;
        }

        return true;
    }

    /**
     * Add a pattern to the list of spam patterns.
     *
     * If later a message is matched against this pattern, the message is
     * considered to be spam and should be blocked.
     *
     * </br>
     *
     * If the pattern is invalid, or the pattern already existed in the
     * blacklist, this method return {@code false}.
     *
     *
     * @param prefs app shared pref.
     * @param pattern the pattern to be added to the blacklist.
     * @return true if the pattern did not exist in the blacklist before, and was a valid pattern.
     */
    public static boolean blockPattern(SharedPreferences prefs, String pattern) {

        final Set<String> patterns = prefs.getStringSet(
                SettingsFragment.BLOCKED_PATTERN, new HashSet<String>(0));

        if(patterns.contains(pattern)) {
            Log.w(TAG, "duplicate pattern: " + pattern);
            return false;
        }
        if(!isValidPattern(pattern)) {
            Log.e(TAG, "invalid pattern: " + pattern);
            return false;
        }

        patterns.add(pattern);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_PATTERN, patterns).apply();
        return true;
    }

    /**
     * Remove a pattern previously defined to be spam pattern.
     *
     * @param prefs app shared pref.
     * @param pattern the pattern to remove from blacklist.
     * @return true if the pattern was actually blocked before.
     */
    public static boolean unblockPattern(SharedPreferences prefs, String pattern) {

        final Set<String> values = prefs.getStringSet(
                SettingsFragment.BLOCKED_PATTERN, new HashSet<String>(0));

        final boolean mod = values.remove(pattern);

        prefs.edit().putStringSet(SettingsFragment.BLOCKED_PATTERN, values).apply();

        return mod;
    }

    /**
     * Collection of all patterns considered to be spam.
     *
     * If a message matches against any of these patterns, then the message is
     * spam and should be blocked.
     *
     * @param prefs app shared pref
     * @return Collection of all patterns considered to be spam.
     */
    public static Collection<String> getBlockedPatterns(SharedPreferences prefs) {

        return prefs.getStringSet(
                SettingsFragment.BLOCKED_PATTERN, Collections.emptySet());
    }

    /**
     * Returns the first spam pattern {@code body} matches against, null if none was found.
     *
     * @param prefs app shared pref.
     * @param body body of the the message to check.
     * @return the first spam pattern body matches against or null if none was found.
     */
    private static String getBlockedPatternOf(SharedPreferences prefs, String body) {

        for (final String pattern : getBlockedPatterns(prefs)) {
            if(pattern == null || pattern.isEmpty()) {
                Log.w(TAG, "skipped empty pattern");
                continue;
            }
            final Pattern regex;
            try {
                regex = Pattern.compile(pattern);
            }
            catch (final Exception e) {
                Log.e(TAG, "pattern skipped, bad regex: " + pattern, e);
                continue;
            }
            if (regex.matcher(body).matches()) {
                return pattern;
            }
        }

        return null;
    }

}

