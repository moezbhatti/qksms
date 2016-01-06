package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.interfaces.LiveView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Allows views to register for updates on preferences
 * <p>
 * Example: A button may need to know when the theme changes, so it that
 * it can change colors accordingly
 * <p>
 * In order to do this, you can use this class as following:
 * LiveViewManager.registerView(QKPreference.THEME, key -> {
 * // Change button color
 * }
 * <p>
 * You won't need to initialize the button color in addition to registering it
 * in the LiveViewManager, because registering it will trigger a refresh automatically,
 * which will initialize it
 */
public abstract class LiveViewManager {
    private static final String TAG = "LiveViewManager";

    /**
     * Maps all of the LiveViews to their associated preference
     */
    private static final HashMap<String, WeakHashMap<Object, Set<LiveView>>> sViews = new HashMap<>();

    /**
     * A list of preferences to be excluded from LiveView refreshing when the preference changes
     */
    private static final HashSet<String> sExcludedPrefs = new HashSet<>(Arrays.asList(
            QKPreference.THEME.getKey(),
            QKPreference.BACKGROUND.getKey()
    ));

    /**
     * Initialize preferences and register a listener for changes
     *
     * @param context Context
     */
    public static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (!sExcludedPrefs.contains(key)) {
                refreshViews(key);
            }
        });
    }

    /**
     * Convenience method for #registerView(QKPreference, ThemedView) to allow registering a single
     * ThemedView to listen for multiple preferences
     *
     * @param view        The LiveView
     * @param parent      The object to tie the lifecycle of the LiveView to. If we only reference
     *                    a LiveView anonymous inner class, then it'll be quickly garbage collected
     *                    and removed from the WeakHashMap. Instead, we should reference the parent
     *                    object (ie. The Activity, Fragment, View, etc...) that this LiveView is
     *                    concerned with. In most cases, it's acceptable to just pass in `this`
     * @param preferences The preferences to listen for
     */
    public static void registerView(LiveView view, Object parent, QKPreference... preferences) {
        for (QKPreference preference : preferences) {
            registerView(preference, parent, view);
        }
    }

    /**
     * Register a view to be updated when a QKPreference is changed
     * We don't need to manually unregister the views because we're using weak sets
     *
     * @param preference The preference to listen for
     * @param parent     The object to tie the lifecycle of the LiveView to. If we only reference
     *                   a LiveView anonymous inner class, then it'll be quickly garbage collected
     *                   and removed from the WeakHashMap. Instead, we should reference the parent
     *                   object (ie. The Activity, Fragment, View, etc...) that this LiveView is
     *                   concerned with. In most cases, it's acceptable to just pass in `this`
     * @param view       The LiveView
     */
    public static void registerView(QKPreference preference, Object parent, LiveView view) {
        synchronized (sViews) {
            if (sViews.containsKey(preference.getKey())) {
                WeakHashMap<Object, Set<LiveView>> parents = sViews.get(preference.getKey());
                if (!parents.containsKey(parent)) {
                    parents.put(parent, new HashSet<>());
                }
                if (!parents.get(parent).contains(view)) {
                    parents.get(parent).add(view);
                }
            } else {
                WeakHashMap<Object, Set<LiveView>> set = new WeakHashMap<>();
                set.put(parent, new HashSet<>());
                set.get(parent).add(view);
                sViews.put(preference.getKey(), set);
            }
        }

        // Fire it off once registered
        view.refresh(preference.getKey());
    }

    /**
     * Refresh all views that are registered to listen for updates to the given preference
     * Convenience method for #refreshViews(String key)
     *
     * @param preference The preference
     */
    public static void refreshViews(QKPreference preference) {
        refreshViews(preference.getKey());
    }

    /**
     * Refresh all views that are registered to listen for updates to the given preference
     * Convenience method for #refreshViews(String key)
     *
     * @param key The preference key
     */
    private static void refreshViews(String key) {
        synchronized (sViews) {
            if (sViews.get(key) != null) {
                for (Set<LiveView> views : sViews.get(key).values()) {
                    for (LiveView view : views) {
                        view.refresh(key);
                    }
                }
            }
        }
    }
}
