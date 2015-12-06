package com.moez.QKSMS.ui.settings;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.mariussoft.endlessjabber.sdk.EndlessJabberInterface;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.AnalyticsManager;
import com.moez.QKSMS.common.DialogHelper;
import com.moez.QKSMS.common.DonationManager;
import com.moez.QKSMS.common.ListviewHelper;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.receiver.NightModeAutoReceiver;
import com.moez.QKSMS.transaction.EndlessJabber;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.ContentFragment;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.dialog.BlockedNumberDialog;
import com.moez.QKSMS.ui.dialog.BubblePreferenceDialog;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.dialog.mms.MMSSetupFragment;
import com.moez.QKSMS.ui.view.QKTextView;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerDialog;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerSwatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener, LiveView, ContentFragment {
    private final String TAG = "PreferenceFragment";

    public static final String CATEGORY_APPEARANCE = "pref_key_category_appearance";
    public static final String CATEGORY_THEME = "pref_category_theme";
    public static final String CATEGORY_GENERAL = "pref_key_category_general";
    public static final String CATEGORY_NOTIFICATIONS = "pref_key_category_notifications";
    public static final String CATEGORY_MMS = "pref_key_category_mms";
    public static final String CATEGORY_QUICKREPLY = "pref_key_category_quickreply";
    public static final String CATEGORY_QUICKCOMPOSE = "pref_key_category_quickcompose";
    public static final String CATEGORY_ADVANCED = "pref_key_category_advanced";
    public static final String CATEGORY_ABOUT = "pref_key_category_about";

    // Sub-categories
    public static final String CATEGORY_APPEARANCE_STATUS_BARS = "pref_key_category_appearance_status_bars";
    /**
     * The category which holds the advanced preferences in settings_main.xml
     */
    public static final String CATEGORY_ADVANCED_HOLDER = "pref_key_category_advanced_holder";

    public static final String THEME = "pref_key_theme";
    public static final String ICON = "pref_key_icon";
    public static final String STATUS_TINT = "pref_key_status_tint";
    public static final String BACKGROUND = "pref_key_background";
    public static final String BUBBLES = "pref_key_bubbles";
    public static final String BUBBLES_NEW = "pref_key_new_bubbles";
    public static final String COLOUR_SENT = "pref_key_colour_sent";
    public static final String COLOUR_RECEIVED = "pref_key_colour_received";
    public static final String HIDE_AVATAR_CONVERSATIONS = "pref_key_hide_avatar_conversations";
    public static final String HIDE_AVATAR_SENT = "pref_key_hide_avatar_sent";
    public static final String HIDE_AVATAR_RECEIVED = "pref_key_hide_avatar_received";
    public static final String AUTO_EMOJI = "pref_key_auto_emoji";
    public static final String MARKDOWN_ENABLED = "pref_key_markdown_enabled";
    public static final String ENTER_BUTTON = "pref_key_enter_button";
    public static final String COMPOSE_FAVORITES = "pref_key_compose_favorites";
    public static final String FONT_FAMILY = "pref_key_font_family";
    public static final String FONT_SIZE = "pref_key_font_size";
    public static final String FONT_WEIGHT = "pref_key_font_weight";
    public static final String MESSAGE_COUNT = "pref_key_message_count";
    public static final String SLIDING_TAB = "pref_key_sliding_tab";
    public static final String PROXIMITY_CALLING = "pref_key_prox_sensor_calling";
    public static final String DELIVERY_REPORTS = "pref_key_delivery";
    public static final String DELIVERY_TOAST = "pref_key_delivery_toast";
    public static final String DELIVERY_VIBRATE = "pref_key_delivery_vibrate";
    public static final String YAPPY = "pref_key_endlessjabber";
    public static final String BLOCKED_ENABLED = "pref_key_blocked_enabled";
    public static final String BLOCKED_SENDERS = "pref_key_blocked_senders";
    public static final String BLOCKED_FUTURE = "pref_key_block_future";
    public static final String MOBILE_ONLY = "pref_key_mobile_only";
    public static final String COMPOSE_GROUP = "pref_key_compose_group";
    public static final String SPLIT_SMS = "pref_key_split";
    public static final String SPLIT_COUNTER = "pref_key_split_counter";
    public static final String LONG_AS_MMS = "pref_key_long_as_mms";
    public static final String LONG_AS_MMS_AFTER = "pref_key_long_as_mms_after";
    public static final String TIMESTAMPS_24H = "pref_key_24h";
    public static final String NOTIFICATIONS = "pref_key_notifications";
    public static final String NOTIFICATION_LED = "pref_key_led";
    public static final String NOTIFICATION_LED_COLOR = "pref_key_theme_led";
    public static final String WAKE = "pref_key_wake";
    public static final String NOTIFICATION_TICKER = "pref_key_ticker";
    public static final String PRIVATE_NOTIFICATION = "pref_key_notification_private";
    public static final String NOTIFICATION_VIBRATE = "pref_key_vibration";
    public static final String NOTIFICATION_TONE = "pref_key_ringtone";
    public static final String NOTIFICATION_CALL_BUTTON = "pref_key_notification_call";
    public static final String DELAYED = "pref_key_delayed";
    public static final String DELAY_DURATION = "pref_key_delay_duration";
    public static final String NIGHT_AUTO = "pref_key_night_auto";
    public static final String DAY_START = "pref_key_day_start";
    public static final String NIGHT_START = "pref_key_night_start";
    public static final String QK_RESPONSES = "pref_key_qk_responses";
    public static final String MMS_ENABLED = "pref_key_mms_enabled";
    public static final String AUTO_DATA = "pref_key_auto_data";
    public static final String MMSC_URL = "mmsc_url";
    public static final String MMS_PORT = "mms_port";
    public static final String MMS_PROXY = "mms_proxy";
    public static final String AUTOMATICALLY_CONFIGURE_MMS = "pref_key_automatically_configure_mms";
    public static final String MMS_CONTACT_SUPPORT = "pref_key_mms_contact_support";
    public static final String SIMPLE_PREFS = "pref_key_simple";
    public static final String DONATE = "pref_key_donate";
    public static final String DISMISSED_READ = "pref_key_dismiss_read";
    /**
     * @deprecated
     */
    public static final String APN_CHOOSER = "pref_key_apn_chooser";
    public static final String MAX_MMS_ATTACHMENT_SIZE = "pref_mms_max_attachment_size";
    public static final String QUICKREPLY = "pref_key_quickreply_enabled";
    public static final String QUICKREPLY_TAP_DISMISS = "pref_key_quickreply_dismiss";
    public static final String QUICKCOMPOSE = "pref_key_quickcompose";
    public static final String STRIP_UNICODE = "pref_key_strip_unicode";
    public static final String SYSTEM_BAR_FLAT = "pref_key_system_bar_flat";
    public static final String STATUS_COMPAT = "pref_key_status_compat";
    public static final String VERSION = "pref_key_version";
    public static final String CHANGELOG = "pref_key_changelog";
    public static final String THANKS = "pref_key_thanks";

    public static final String WELCOME_SEEN = "pref_key_welcome_seen";

    public static final String DEFAULT_NOTIFICATION_TONE = "content://settings/system/notification_sound";

    public static final String CATEGORY_TAG = "settings_category_fragment_tag";

    private MainActivity mContext;
    private PreferenceManager mPreferenceManager;
    private SharedPreferences mPrefs;
    private Resources mRes;
    private ListView mListView;

    private Preference mThemeLed;
    private EditTextPreference mMmscUrl;
    private EditTextPreference mMmsProxy;
    private EditTextPreference mMmsPort;

    private ColorPickerDialog mLedColorPickerDialog;

    private String[] mFontFamilies;
    private String[] mFontSizes;
    private String[] mFontWeights;
    private int[] mLedColors;

    private ListPreference mMaxMmsAttachmentSize;
    private String[] mMaxMmsAttachmentSizes;

    private int mResource;

    public static SettingsFragment newInstance(int category) {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putInt("category", category);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mContext = (MainActivity) getActivity();
        mPrefs = mContext.getPrefs();
        mRes = mContext.getResources();

        mResource = args.getInt("category", R.xml.settings);
        if (mResource == R.xml.settings_simple || mResource == R.xml.settings_main) {
            mResource = mPrefs.getBoolean(SIMPLE_PREFS, true) ? R.xml.settings_simple : R.xml.settings_main;
        }
        addPreferencesFromResource(mResource);

        // Set `this` to be the preferences click/change listener for all the preferences.
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            pref.setOnPreferenceClickListener(this);
            pref.setOnPreferenceChangeListener(this);

            // If this is a preference category, make sure to go through all the subpreferences as
            // well.
            if (pref instanceof PreferenceCategory) {
                Stack<PreferenceCategory> stack = new Stack<>();
                stack.push((PreferenceCategory) pref);

                do {
                    PreferenceCategory category = stack.pop();
                    for (int j = 0; j < category.getPreferenceCount(); j++) {
                        Preference subPref = category.getPreference(j);
                        subPref.setOnPreferenceClickListener(this);
                        subPref.setOnPreferenceChangeListener(this);

                        if (subPref instanceof PreferenceCategory) {
                            stack.push((PreferenceCategory) subPref);
                        }
                    }
                } while (!stack.isEmpty());
            }
        }

        Preference icon = findPreference(ICON);
        if (icon != null) {
            icon.setOnPreferenceClickListener(this);
        }

        CheckBoxPreference status_tint = (CheckBoxPreference) findPreference(STATUS_TINT);
        if (status_tint != null) {
            status_tint.setOnPreferenceChangeListener(this);
        }

        mThemeLed = findPreference(NOTIFICATION_LED_COLOR);
        if (mThemeLed != null) {

            mLedColors = new int[]{
                    mRes.getColor(R.color.blue_light), mRes.getColor(R.color.purple_light),
                    mRes.getColor(R.color.green_light), mRes.getColor(R.color.yellow_light),
                    mRes.getColor(R.color.red_light), mRes.getColor(R.color.white_pure)
            };

            mLedColorPickerDialog = new ColorPickerDialog();
            mLedColorPickerDialog.initialize(R.string.pref_theme_led, mLedColors, Integer.parseInt(mPrefs.getString(NOTIFICATION_LED_COLOR, "-48060")), 3, 2);
            mLedColorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                @Override
                public void onColorSelected(int color) {
                    mPrefs.edit().putString(mThemeLed.getKey(), "" + color).apply();
                    onPreferenceChange(findPreference(mThemeLed.getKey()), color);
                }
            });
        }

        ListPreference font_family = (ListPreference) findPreference(FONT_FAMILY);
        if (font_family != null) {
            mFontFamilies = mRes.getStringArray(R.array.font_families);
            font_family.setSummary(mFontFamilies[Integer.parseInt(font_family.getValue())]);
        }

        ListPreference font_size = (ListPreference) findPreference(FONT_SIZE);
        if (font_size != null) {
            mFontSizes = mRes.getStringArray(R.array.font_sizes);
            font_size.setSummary(mFontSizes[Integer.parseInt(font_size.getValue())]);
        }

        ListPreference font_weight = (ListPreference) findPreference(FONT_WEIGHT);
        if (font_weight != null) {
            mFontWeights = mRes.getStringArray(R.array.font_weights);
            int i = Integer.parseInt(font_weight.getValue());
            font_weight.setSummary(mFontWeights[i == 2 ? 0 : 1]);
        }

        Preference day_start = findPreference(DAY_START);
        if (day_start != null) {
            day_start.setSummary(DateFormatter.getSummaryTimestamp(mContext, mPrefs.getString(DAY_START, "6:00")));
        }

        Preference night_start = findPreference(NIGHT_START);
        if (night_start != null) {
            night_start.setSummary(DateFormatter.getSummaryTimestamp(mContext, mPrefs.getString(NIGHT_START, "21:00")));
        }

        mMmscUrl = (EditTextPreference) findPreference(MMSC_URL);
        if (mMmscUrl != null) {
            mMmscUrl.setSummary(mMmscUrl.getText());
        }

        mMmsProxy = (EditTextPreference) findPreference(MMS_PROXY);
        if (mMmsProxy != null) {
            mMmsProxy.setSummary(mMmsProxy.getText());
        }

        mMmsPort = (EditTextPreference) findPreference(MMS_PORT);
        if (mMmsPort != null) {
            mMmsPort.setSummary(mMmsPort.getText());
        }

        mMaxMmsAttachmentSize = (ListPreference) findPreference(MAX_MMS_ATTACHMENT_SIZE);
        if (mMaxMmsAttachmentSize != null) {
            mMaxMmsAttachmentSizes = mRes.getStringArray(R.array.max_mms_attachment_sizes);

            String value = mMaxMmsAttachmentSize.getValue();
            String summary = mMaxMmsAttachmentSizes[mMaxMmsAttachmentSize.findIndexOfValue(value)];
            mMaxMmsAttachmentSize.setSummary(summary);
        }

        Preference version = findPreference(VERSION);
        if (version != null) {
            String v = "unknown";
            try {
                PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                v = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            version.setSummary(v);
        }

        // Status and nav bar tinting are only supported on kit kat or above.
        if (!hasKitKat()) {
            removePreference(CATEGORY_APPEARANCE_STATUS_BARS);
            removePreference(CATEGORY_ADVANCED_HOLDER);
        }

    }

    private boolean hasKitKat() {
        return Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT;
    }

    /**
     * Removed top level preferences from the layout
     */
    private void removePreference(String key) {
        Preference pref = findPreference(key);
        PreferenceScreen screen = getPreferenceScreen();
        if (pref != null && screen != null) {
            screen.removePreference(pref);
        }
    }

    /**
     * Removes a preference from a given category
     */
    private void removePreference(String key, String category) {
        Preference pref = findPreference(key);
        PreferenceCategory cat = (PreferenceCategory) findPreference(category);
        if (pref != null && cat != null) {
            cat.removePreference(pref);
        }
    }

    private void setPreferenceEnabled(String key, boolean enabled) {
        Preference pref = findPreference(key);
        if (pref != null) {
            pref.setEnabled(enabled);
        } else {
            Log.w(TAG, "null preference for key " + key + ", can't set enabled to " + enabled);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardUtils.hide(mContext, view);
        mListView = (ListView) view.findViewById(android.R.id.list);

        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);
        refresh();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();

        String valueString = newValue == null ? "null" : newValue.toString();
        if (key.equals(NOTIFICATION_LED_COLOR)) {
            // Format the color as a nice string if it's the LED color.
            valueString = ThemeManager.getColorString(Integer.parseInt(valueString));
        }

        Log.d(TAG, "onPreferenceChange key:" + key + " newValue: " + valueString);
        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_PREFERENCE_CHANGE,
                key,
                valueString
        );

        switch (key) {
            case BACKGROUND:
                ThemeManager.setTheme(ThemeManager.Theme.fromString((String) newValue));
                break;
            case STATUS_TINT:
                ThemeManager.setStatusBarTintEnabled((Boolean) newValue);
                break;
            case FONT_FAMILY:
                preference.setSummary(mFontFamilies[Integer.parseInt("" + newValue)]);
                break;
            case FONT_SIZE:
                preference.setSummary(mFontSizes[Integer.parseInt("" + newValue)]);
                break;
            case FONT_WEIGHT:
                int i = Integer.parseInt("" + newValue);
                preference.setSummary(mFontWeights[i == 2 ? 0 : 1]);
                break;
            case COLOUR_SENT:
                ThemeManager.setSentBubbleColored((Boolean) newValue);
                break;
            case COLOUR_RECEIVED:
                ThemeManager.setReceivedBubbleColored((Boolean) newValue);
                break;
            case NIGHT_AUTO:
                updateAlarmManager(mContext, (Boolean) newValue);
                break;
            case DAY_START:
            case NIGHT_START:
                updateAlarmManager(mContext, true);
                break;
            case SLIDING_TAB:
                mContext.setSlidingTabEnabled((Boolean) newValue);
                break;
            case YAPPY:
                if ((Boolean) newValue) {
                    try {
                        EndlessJabberInterface.EnableIntegration(mContext, EndlessJabber.class, true, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!EndlessJabberInterface.IsInstalled(mContext)) {
                        EndlessJabberInterface.OpenGooglePlayLink(mContext);
                    }
                } else {
                    try {
                        EndlessJabberInterface.DisableIntegration(mContext, EndlessJabber.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case QUICKCOMPOSE:
                NotificationManager.initQuickCompose(getActivity(), (Boolean) newValue, !(Boolean) newValue);
                break;
            case MMSC_URL:
            case MMS_PROXY:
            case MMS_PORT:
                preference.setSummary(newValue.toString());
                break;
            case MAX_MMS_ATTACHMENT_SIZE:
                // Update the summary in the list preference
                ListPreference listpref = (ListPreference) preference;
                int index = listpref.findIndexOfValue((String) newValue);
                preference.setSummary(mMaxMmsAttachmentSizes[index]);
                // Update the SMS helper static class with the new option
                SmsHelper.setMaxAttachmentSizeSetting(mContext, (String) newValue);
                break;
            case SYSTEM_BAR_FLAT:
                ThemeManager.setSystemBarFlatEnabled((Boolean) newValue);
                break;
            case STATUS_COMPAT:
                ThemeManager.setStatusBarTintCompat((Boolean) newValue);
                break;
            case DELAY_DURATION:
                try {
                    int duration = Integer.parseInt((String) newValue);

                    if (duration < 1 || duration > 30)
                        throw new Exception("Duration out of bounds");

                } catch (Exception e) {
                    Toast.makeText(mContext, R.string.delayed_duration_bounds_error, Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        String key = preference.getKey() != null ? preference.getKey() : "";

        AnalyticsManager.getInstance().sendEvent(AnalyticsManager.CATEGORY_PREFERENCE_CLICK, key, null);

        // Categories
        int resId = 0;
        switch (key) {
            case CATEGORY_APPEARANCE:
                resId = R.xml.settings_appearance;
                break;
            case CATEGORY_GENERAL:
                resId = R.xml.settings_general;
                break;
            case CATEGORY_NOTIFICATIONS:
                resId = R.xml.settings_notifications;
                break;
            case CATEGORY_MMS:
                resId = R.xml.settings_mms;
                break;
            case CATEGORY_QUICKREPLY:
                resId = R.xml.settings_quickreply;
                break;
            case CATEGORY_QUICKCOMPOSE:
                resId = R.xml.settings_quickcompose;
                break;
            case CATEGORY_ADVANCED:
                resId = R.xml.settings_advanced;
                break;
            case CATEGORY_ABOUT:
                resId = R.xml.settings_about;
                break;
        }
        if (resId != 0) {
            Fragment fragment = SettingsFragment.newInstance(resId);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_frame, fragment, CATEGORY_TAG)
                    .commit();
        }

        switch (key) {
            case THEME:
                ThemeManager.showColourSwatchesDialog(mContext);
                break;
            case BUBBLES:
                new BubblePreferenceDialog().setContext(mContext).show();
                break;
            case ICON:
                ThemeManager.setIcon(mContext);
                break;
            case BLOCKED_FUTURE:
                BlockedNumberDialog.showDialog(mContext);
                break;
            case NOTIFICATION_LED_COLOR:
                mLedColorPickerDialog.show(getActivity().getFragmentManager(), "colorpicker");
                break;
            case DAY_START:
            case NIGHT_START:
                TimePickerFragment fragment = new TimePickerFragment();
                fragment.setPreference(preference);
                fragment.setOnPreferenceChangeListener(this);
                fragment.show(getFragmentManager(), "timepicker");
                break;
            case QK_RESPONSES:
                showQkResponseEditor();
                break;
            case AUTOMATICALLY_CONFIGURE_MMS:
                // Show the MMS setup dialogs. See the MMSSetupDialog class for info about what the
                // arguments mean.
                MMSSetupFragment f = new MMSSetupFragment();
                Bundle args = new Bundle();
                args.putBoolean(MMSSetupFragment.ARG_ASK_FIRST, false);
                args.putString(MMSSetupFragment.ARG_DONT_ASK_AGAIN_PREF, null);
                f.setArguments(args);

                getFragmentManager().beginTransaction()
                        .add(f, MMSSetupFragment.TAG)
                        .commit();
                break;
            case MMS_CONTACT_SUPPORT:
                // Opens an email compose intent with MMS debugging information
                MMSSetupFragment.contactSupport(getActivity());
                break;
            case CHANGELOG:
                DialogHelper.showChangelog(mContext);
                break;
            case THANKS:
                new QKDialog()
                        .setContext(mContext)
                        .setTitle(R.string.pref_about_thanks_title)
                        .setTripleLineItems(R.array.contributor_names, R.array.contributor_githubs, R.array.contributor_projects,
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        String baseUrl = ((QKTextView) view.findViewById(R.id.list_item_subtitle)).getText().toString();
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + baseUrl));
                                        startActivity(browserIntent);
                                    }
                                })
                        .show();
                break;
            case SIMPLE_PREFS:
                mContext.onOptionsItemSelected(mContext.getMenu().findItem(R.id.simple_settings));
                break;
            case DONATE:
                DonationManager.getInstance(mContext).showDonateDialog();
                break;
        }

        return false;
    }

    public boolean isCategoryList() {
        return mResource == R.xml.settings_main;
    }

    public void showQkResponseEditor() {

        Set<String> defaultResponses = new HashSet<>(Arrays.asList(mContext.getResources().getStringArray(R.array.qk_responses)));
        Set<String> responseSet = mPrefs.getStringSet(SettingsFragment.QK_RESPONSES, defaultResponses);
        ArrayList<String> responses = new ArrayList<>();
        responses.addAll(responseSet);
        Collections.sort(responses);
        for (int i = responses.size(); i < 12; i++) {
            responses.add("");
        }

        final QKResponseAdapter adapter = new QKResponseAdapter(mContext, R.layout.list_item_qk_response, responses);
        ListView listView = new ListView(mContext);
        listView.setDividerHeight(0);
        listView.setAdapter(adapter);

        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.title_qk_responses)
                .setCustomView(listView)
                .setPositiveButton(R.string.save, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPrefs.edit().putStringSet(SettingsFragment.QK_RESPONSES, new HashSet<>(adapter.getResponses())).apply();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show(getFragmentManager(), "qk_response");
    }

    public static void updateAlarmManager(Context context, boolean enabled) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm");
        Calendar calendar = Calendar.getInstance();

        Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTimeInMillis(System.currentTimeMillis());
        try {
            calendar.setTime(simpleDateFormat.parse(PreferenceManager.getDefaultSharedPreferences(context).getString(DAY_START, "6:00")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dayCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        dayCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        Intent day = new Intent(context, NightModeAutoReceiver.class);
        PendingIntent dayIntent = PendingIntent.getBroadcast(context, 0, day, 0);

        Calendar nightCalendar = Calendar.getInstance();
        nightCalendar.setTimeInMillis(System.currentTimeMillis());
        try {
            calendar.setTime(simpleDateFormat.parse(PreferenceManager.getDefaultSharedPreferences(context).getString(NIGHT_START, "21:00")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        nightCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        nightCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        Intent night = new Intent(context, NightModeAutoReceiver.class);
        PendingIntent nightIntent = PendingIntent.getBroadcast(context, 1, night, 0);

        context.sendBroadcast(night);

        if (enabled) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, dayCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, dayIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nightCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, nightIntent);
        } else {
            alarmManager.cancel(dayIntent);
            alarmManager.cancel(nightIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LiveViewManager.unregisterView(this);
    }

    @Override
    public void refresh() {
        ListviewHelper.applyCustomScrollbar(mContext, mListView);

        View view = getView();
        if (view != null) {
            view.setBackgroundColor(ThemeManager.getBackgroundColor());
        }
    }

    @Override
    public void onContentOpening() {

    }

    @Override
    public void onContentOpened() {

    }

    @Override
    public void onContentClosing() {

    }

    @Override
    public void onContentClosed() {

    }

    @Override
    public void inflateToolbar(Menu menu, MenuInflater inflater, Context context) {
        if (mContext == null) {
            mContext = (MainActivity) context;
            mPrefs = mContext.getPrefs();
        }

        inflater.inflate(R.menu.settings, menu);
        mContext.setTitle(R.string.title_settings);

        MenuItem simplePrefs = menu.findItem(R.id.simple_settings);
        if (mPrefs.getBoolean(SettingsFragment.SIMPLE_PREFS, true)) {
            simplePrefs.setTitle(R.string.menu_show_all_prefs);
        } else {
            simplePrefs.setTitle(R.string.menu_show_fewer_prefs);
        }
    }
}
