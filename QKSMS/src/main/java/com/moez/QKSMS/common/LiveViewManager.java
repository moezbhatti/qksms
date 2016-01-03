package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.moez.QKSMS.common.preferences.QKPreference;
import com.moez.QKSMS.interfaces.LiveView;

import java.util.Collections;
import java.util.HashMap;
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
    private static final String TAG = "ThemedViewManager";

    private static final HashMap<String, Set<LiveView>> sViews = new HashMap<>();

    /**
     * Initialize preferences and register a listener for changes
     *
     * @param context Context
     */
    public static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> refreshViews(key));
    }

    /**
     * Convenience method for #registerView(QKPreference, ThemedView) to allow registering a single
     * ThemedView to listen for multiple preferences
     *
     * @param view        The ThemedView
     * @param preferences The preferences to listen for
     */
    public static void registerView(LiveView view, QKPreference... preferences) {
        for (QKPreference preference : preferences) {
            registerView(preference, view);
        }
    }

    /**
     * Register a view to be updated when a QKPreference is changed
     * We don't need to manually unregister the views because we're using weak sets
     *
     * @param preference The preference to listen for
     * @param view       The view
     */
    public static void registerView(QKPreference preference, LiveView view) {
        synchronized (sViews) {
            if (sViews.containsKey(preference.getKey())) {
                Set<LiveView> views = sViews.get(preference.getKey());
                views.add(view);
            } else {
                Set<LiveView> set = Collections.newSetFromMap(new WeakHashMap<>());
                set.add(view);
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
        Set<LiveView> toRefresh = sViews.get(key);

        // Refresh those views.
        if (toRefresh != null) {
            for (LiveView view : toRefresh) {
                view.refresh(key);
            }
        }
    }
}
