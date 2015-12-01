package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.moez.QKSMS.interfaces.LiveView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Allows LiveViews to register for updates on relevant preferences.
 */
public class LiveViewManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "LiveViewManager";
    private static final boolean LOCAL_LOGV = false;

    private static LiveViewManager sInstance;

    private static final Set<LiveView> sSet = Collections.newSetFromMap(new WeakHashMap<LiveView, Boolean>());
    private static final WeakHashMap<LiveView, Set<String>> sPrefsMap = new WeakHashMap<>();

    /**
     * Private constructor.
     */
    private LiveViewManager() {}

    /**
     * Initialize a static instance so that we can listen for views.
     */
    static {
        sInstance = new LiveViewManager();
    }

    public static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(sInstance);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        LiveViewManager.refreshViews(key);
    }

    /**
     * Registers a LiveView for global updates. To getConversation updates for specific preferences, use
     * registerPreference.
     * @param v
     */
    public static void registerView(LiveView v) {
        synchronized (sSet) {
            sSet.add(v);
        }

        synchronized(sPrefsMap) {
            // Don't add the view to the prefs map more than once, in case the LiveView has already
            // been initalized.
            if (!sPrefsMap.containsKey(v)) {
                sPrefsMap.put(v, new HashSet<String>());
            }
        }
    }

    /**
     * Unregisters a LiveView for any updates: global, or preference-specific.
     * @param v
     */
    public static void unregisterView(LiveView v) {
        synchronized (sSet) {
            sSet.remove(v);
        }

        synchronized (sPrefsMap) {
            sPrefsMap.remove(v);
        }
    }

    /**
     * Register a LiveView to be notified when this preference is updated.
     * Note that you must first register the view, otherwise #refresh() will
     * not be called on it
     *
     * @param v
     * @param pref
     */
    public static void registerPreference(LiveView v, String pref) {
        synchronized (sPrefsMap) {
            Set<String> prefs = sPrefsMap.get(v);
            // WeakHashSet: the value might have been removed.
            if (prefs != null) {
                prefs.add(pref);
            }
        }
    }

    /**
     * Register a LiveView to be notified when this preference is updated.
     *
     * @param v
     * @param pref
     */
    public static void unregisterPreference(LiveView v, String pref) {
        synchronized (sPrefsMap) {
            Set<String> prefs = sPrefsMap.get(v);
            // WeakHashSet: the value might have been removed.
            if (prefs != null) {
                prefs.remove(pref);
            }
        }
    }

    /**
     * Refresh all views.
     */
    public static void refreshViews() {
        synchronized (sSet) {
            for (LiveView view : sSet) {
                view.refresh();
            }
        }
    }

    /**
     * Refreshes only the views that are listening for any of the given preferences.
     * @param query
     */
    public static void refreshViews(String... query) {
        Set<LiveView> toRefresh = getViews(query);

        // Refresh those views.
        for (LiveView view : toRefresh) {
            view.refresh();
        }
    }

    /**
     * Returns the set of LiveViews which subscribe to at least one of the given preferences. Can
     * be used to build a quick cache of LiveViews for rapid refreshing, i.e. animations.
     *
     * @param query
     * @return
     */
    public static Set<LiveView> getViews(String... query) {
        // Build a set of the given preferences.
        Set<String> querySet = new HashSet<>();
        for (String string : query) {
            querySet.add(string);
        }

        // Get all the views that have at least one of the changed preferences.
        Set<LiveView> result = new HashSet<>();
        synchronized (sPrefsMap) {
            for (LiveView view : sPrefsMap.keySet()) {
                Set<String> viewPrefs = sPrefsMap.get(view);
                if (viewPrefs != null && !Collections.disjoint(querySet, viewPrefs)) {
                    result.add(view);
                }
            }
        }

        if (LOCAL_LOGV) Log.v(TAG, "getViews returned:" + result.size() + " for preferences:" + querySet);
        return result;
    }
}
