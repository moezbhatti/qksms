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
import android.os.Handler;
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
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.common.utils.PackageUtils;
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
        Preference.OnPreferenceClickListener, ContentFragment {
    private final String TAG = "PreferenceFragment";

    public static final String CATEGORY_APPEARANCE = "pref_key_category_appearance";
    public static final String CATEGORY_THEME = "pref_category_theme";
    public static final String CATEGORY_GENERAL = "pref_key_category_general";
    public static final String CATEGORY_NOTIFICATIONS = "pref_key_category_notifications";
    public static final String CATEGORY_MMS = "pref_key_category_mms";
    public static final String CATEGORY_QUICKREPLY = "pref_key_category_quickreply";
    public static final String CATEGORY_QUICKCOMPOSE = "pref_key_category_quickcompose";
    public static final String CATEGORY_ABOUT = "pref_key_category_about";

    // Sub-categories
    public static final String CATEGORY_APPEARANCE_SYSTEM_BARS = "pref_key_category_appearance_system_bars";

    public static final String THEME = "pref_key_theme";
    public static final String ICON = "pref_key_icon";
    public static final String STATUS_TINT = "pref_key_status_tint";
    public static final String NAVIGATION_TINT = "pref_key_navigation_tint";
    public static final String BACKGROUND = "pref_key_background";
    public static final String BUBBLES = "pref_key_bubbles";
    public static final String BUBBLES_NEW = "pref_key_new_bubbles";
    public static final String COLOR_SENT = "pref_key_colour_sent";
    public static final String COLOR_RECEIVED = "pref_key_colour_received";
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
    public static final String SHOULD_I_ANSWER = "pref_key_should_i_answer";
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
    public static final String DONATE = "pref_key_donate";
    public static final String DISMISSED_READ = "pref_key_dismiss_read";
    public static final String MAX_MMS_ATTACHMENT_SIZE = "pref_mms_max_attachment_size";
    public static final String QUICKREPLY = "pref_key_quickreply_enabled";
    public static final String QUICKREPLY_TAP_DISMISS = "pref_key_quickreply_dismiss";
    public static final String QUICKCOMPOSE = "pref_key_quickcompose";
    public static final String STRIP_UNICODE = "pref_key_strip_unicode";
    public static final String VERSION = "pref_key_version";
    public static final String CHANGELOG = "pref_key_changelog";
    public static final String THANKS = "pref_key_thanks";
    public static final String GOOGLE_PLUS = "pref_key_google_plus";
    public static final String GITHUB = "pref_key_github";
    public static final String CROWDIN = "pref_key_crowdin";

    public static final String WELCOME_SEEN = "pref_key_welcome_seen";

    public static final String DEFAULT_NOTIFICATION_TONE = "content://settings/system/notification_sound";

    public static final String CATEGORY_TAG = "settings_category_fragment_tag";

    public static final String GOOGLE_PLUS_URL = "https://plus.google.com/communities/104505769539048913485";
    public static final String GITHUB_URL = "https://github.com/qklabs/qksms";
    public static final String CROWDIN_URL = "https://crowdin.com/project/qksms";

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            removePreference(CATEGORY_APPEARANCE_SYSTEM_BARS);
        }
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

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            ListviewHelper.applyCustomScrollbar(mContext, mListView);

            View view1 = getView();
            if (view1 != null) {
                view1.setBackgroundColor(ThemeManager.getBackgroundColor());
            }
        });
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
                ThemeManager.setStatusBarTintEnabled(mContext, (Boolean) newValue);
                break;
            case NAVIGATION_TINT:
                ThemeManager.setNavigationBarTintEnabled(mContext, (Boolean) newValue);
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
            case COLOR_SENT:
                ThemeManager.setSentBubbleColored((Boolean) newValue);
                break;
            case COLOR_RECEIVED:
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
            case CATEGORY_ABOUT:
                resId = R.xml.settings_about;
                break;
        }
        if (resId != 0) {
            Fragment fragment = SettingsFragment.newInstance(resId);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment, CATEGORY_TAG)
                    .commit();
        }

        switch (key) {
            case THEME:
                ThemeManager.showColorPickerDialog(mContext);
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
            case SHOULD_I_ANSWER:
                final String packageName = "org.mistergroup.muzutozvednout";
                if (!PackageUtils.isAppInstalled(mContext, packageName)) {
                    String referrer="referrer=utm_source%3Dqksms%26utm_medium%3Dapp%26utm_campaign%3Dqksmssettings";
                    new QKDialog()
                            .setContext(mContext)
                            .setTitle(R.string.dialog_should_i_answer_title)
                            .setMessage(R.string.dialog_should_i_answer_message)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.okay, v -> {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName + "&" + referrer)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName + "&" + referrer)));
                                }
                            })
                            .show();

                    new Handler().postDelayed(() -> {
                        mPrefs.edit().putBoolean(SHOULD_I_ANSWER, false).commit();
                        ((CheckBoxPreference) preference).setChecked(false);
                    }, 500);
                }
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
                                        startBrowserIntent("https://" + baseUrl);
                                    }
                                })
                        .show();
                break;
            case DONATE:
                DonationManager.getInstance(mContext).showDonateDialog();
                break;
            case GOOGLE_PLUS:
                startBrowserIntent(GOOGLE_PLUS_URL);
                break;
            case GITHUB:
                startBrowserIntent(GITHUB_URL);
                break;
            case CROWDIN:
                startBrowserIntent(CROWDIN_URL);
                break;
        }

        return false;
    }

    private void startBrowserIntent(final String baseUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(baseUrl));
        startActivity(browserIntent);
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
    public void onMenuChanging(float percentOpen) {

    }

    @Override
    public void inflateToolbar(Menu menu, MenuInflater inflater, Context context) {
        if (mContext == null) {
            mContext = (MainActivity) context;
            mPrefs = mContext.getPrefs();
        }

        inflater.inflate(R.menu.settings, menu);
        mContext.setTitle(R.string.title_settings);
    }
}
