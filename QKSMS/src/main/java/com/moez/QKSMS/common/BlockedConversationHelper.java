package com.moez.QKSMS.common;

import android.content.SharedPreferences;
import android.provider.Telephony;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.util.HashSet;
import java.util.Set;

/**
 * A set of helper methods to group the logic related to blocked conversation
 */
public class BlockedConversationHelper {

    public static boolean isConversationBlocked(SharedPreferences prefs, long threadId) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        HashSet<Long> ids = new HashSet<>();
        for (String s : idStrings) {
            ids.add(Long.parseLong(s));
        }
        return (ids.contains(threadId));
    }

    public static void blockConversation(SharedPreferences prefs, long threadId) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        idStrings.add(String.valueOf(threadId));
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_SENDERS, idStrings).apply();
    }

    public static void unblockConversation(SharedPreferences prefs, long threadId) {
        Set<String> idStrings2 = prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
        idStrings2.remove(String.valueOf(threadId));
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_SENDERS, idStrings2).apply();
    }

    public static Set<String> getBlockedConversations(SharedPreferences prefs) {
        return prefs.getStringSet(SettingsFragment.BLOCKED_SENDERS, new HashSet<String>());
    }

    public static String[] getBlockedConversationArray(SharedPreferences prefs) {
        Set<String> idStrings = getBlockedConversations(prefs);
        Object[] idArray = idStrings.toArray();
        String[] idStringArray = new String[idStrings.size()];
        for (int i = 0; i < idStrings.size(); i++) {
            idStringArray[i] = (String) idArray[i];
        }
        return idStringArray;
    }

    public static String getCursorSelection(SharedPreferences prefs) {
        StringBuilder selection = new StringBuilder();
        selection.append(Telephony.Threads.MESSAGE_COUNT);
        selection.append(" != 0");
        selection.append(" AND ");
        selection.append(Telephony.Threads._ID);
        selection.append(" NOT IN (");

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
}
