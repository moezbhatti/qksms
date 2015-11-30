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
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * A set of helper methods to group the logic related to blocked conversation
 */
public class BlockedConversationHelper {

    public static boolean isConversationBlocked(SharedPreferences prefs, long threadId) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        return (idStrings.contains(String.valueOf(threadId)));
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

    public static void blockFutureConversation(SharedPreferences prefs, String address) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_FUTURE, new HashSet<String>());
        idStrings.add(address);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_FUTURE, idStrings).apply();
    }

    public static void unblockFutureConversation(SharedPreferences prefs, String address) {
        Set<String> idStrings2 = prefs.getStringSet(SettingsFragment.BLOCKED_FUTURE, new HashSet<String>());
        idStrings2.remove(address);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_FUTURE, idStrings2).apply();
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
}
