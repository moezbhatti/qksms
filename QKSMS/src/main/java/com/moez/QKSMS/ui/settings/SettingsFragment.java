package com.moez.QKSMS.ui.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.mariussoft.endlessjabber.sdk.EndlessJabberInterface;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.AnalyticsManager;
import com.moez.QKSMS.common.DialogHelper;
import com.moez.QKSMS.common.DonationManager;
import com.moez.QKSMS.common.ListviewHelper;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.NotificationManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.SmsHelper;
import com.moez.QKSMS.common.ThemeManager;
import com.moez.QKSMS.common.YappyImplementation;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.common.utils.KeyboardUtils;
import com.moez.QKSMS.common.utils.PackageUtils;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.receiver.NightModeAutoReceiver;
import com.moez.QKSMS.service.DeleteOldMessagesService;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.BlockedNumberDialog;
import com.moez.QKSMS.ui.dialog.MMSSetupFragment;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.view.QKTextView;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static com.moez.QKSMS.enums.QKPreference.AUTO_NIGHT_DAY_START;
import static com.moez.QKSMS.enums.QKPreference.AUTO_NIGHT_NIGHT_START;
import static com.moez.QKSMS.enums.QKPreference.ICON;
import static com.moez.QKSMS.enums.QKPreference.NOTIFICATIONS_LED_COLOR;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    public static final String TAG = "SettingsFragment";

    public static final String CATEGORY_APPEARANCE = "pref_key_category_appearance";
    public static final String CATEGORY_APPEARANCE_SYSTEM_BARS = "pref_key_category_appearance_system_bars";
    public static final String CATEGORY_THEME = "pref_category_theme";
    public static final String CATEGORY_GENERAL = "pref_key_category_general";
    public static final String CATEGORY_NOTIFICATIONS = "pref_key_category_notifications";
    public static final String CATEGORY_MMS = "pref_key_category_mms";
    public static final String CATEGORY_QUICKREPLY = "pref_key_category_quickreply";
    public static final String CATEGORY_QUICKCOMPOSE = "pref_key_category_quickcompose";
    public static final String CATEGORY_ABOUT = "pref_key_category_about";
    public static final String CATEGORY_TAG = "settings_category_fragment_tag";

    public static final String GOOGLE_PLUS_URL = "https://plus.google.com/communities/104505769539048913485";
    public static final String GITHUB_URL = "https://github.com/qklabs/qksms";
    public static final String CROWDIN_URL = "https://crowdin.com/project/qksms";

    private QKActivity mContext;
    private ListView mListView;

    private ColorPickerDialog mLedColorPickerDialog;

    private String[] mFontFamilies;
    private String[] mFontSizes;
    private String[] mFontWeights;

    private ListPreference mMaxMmsAttachmentSize;
    private String[] mMaxMmsAttachmentSizes;

    private int mResource;

    protected static SettingsFragment newInstance(int category) {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putInt(SettingsActivity.ARG_SETTINGS_PAGE, category);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);

        mContext = (QKActivity) getActivity();
        Resources res = mContext.getResources();

        mResource = args.getInt(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings);
        addPreferencesFromResource(mResource);

        mContext.setTitle(R.string.title_settings);
        switch (mResource) {
            case R.xml.settings_main:
                mContext.setTitle(R.string.title_settings);
                break;

            case R.xml.settings_appearance:
                mContext.setTitle(R.string.pref_category_appearance);
                break;

            case R.xml.settings_general:
                mContext.setTitle(R.string.pref_category_general);
                break;

            case R.xml.settings_notifications:
                mContext.setTitle(R.string.pref_category_notifications);
                break;

            case R.xml.settings_mms:
                mContext.setTitle(R.string.pref_category_mms);
                break;

            case R.xml.settings_quickreply:
                mContext.setTitle(R.string.pref_category_quickreply);
                break;

            case R.xml.settings_quickcompose:
                mContext.setTitle(R.string.pref_category_quickcompose);
                break;

            case R.xml.settings_about:
                mContext.setTitle(R.string.pref_category_about);
                break;
        }

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

        Preference icon = findPreference(ICON.getKey());
        if (icon != null) {
            icon.setOnPreferenceClickListener(this);
        }

        Preference notificationLedColor = findPreference(QKPreference.NOTIFICATIONS_LED_COLOR.getKey());
        if (notificationLedColor != null) {
            int[] ledColors = new int[]{
                    res.getColor(R.color.blue_light), res.getColor(R.color.purple_light),
                    res.getColor(R.color.green_light), res.getColor(R.color.yellow_light),
                    res.getColor(R.color.red_light), res.getColor(R.color.white_pure)
            };

            mLedColorPickerDialog = new ColorPickerDialog();
            mLedColorPickerDialog.initialize(R.string.pref_theme_led, ledColors, Integer.parseInt(QKPreferences.getString(NOTIFICATIONS_LED_COLOR)), 3, 2);
            mLedColorPickerDialog.setOnColorSelectedListener(color -> {
                QKPreferences.putString(NOTIFICATIONS_LED_COLOR, String.valueOf(color));
                onPreferenceChange(findPreference(notificationLedColor.getKey()), color);
            });
        }

        ListPreference fontFamily = (ListPreference) findPreference(QKPreference.FONT_FAMILY.getKey());
        if (fontFamily != null) {
            mFontFamilies = res.getStringArray(R.array.font_families);
            fontFamily.setSummary(mFontFamilies[Integer.parseInt(fontFamily.getValue())]);
        }

        ListPreference fontSize = (ListPreference) findPreference(QKPreference.FONT_SIZE.getKey());
        if (fontSize != null) {
            mFontSizes = res.getStringArray(R.array.font_sizes);
            fontSize.setSummary(mFontSizes[Integer.parseInt(fontSize.getValue())]);
        }

        ListPreference fontWeight = (ListPreference) findPreference(QKPreference.FONT_WEIGHT.getKey());
        if (fontWeight != null) {
            mFontWeights = res.getStringArray(R.array.font_weights);
            int i = Integer.parseInt(fontWeight.getValue());
            fontWeight.setSummary(mFontWeights[i == 2 ? 0 : 1]);
        }

        EditTextPreference deleteUnread = (EditTextPreference) findPreference(QKPreference.AUTO_DELETE_UNREAD.getKey());
        if (deleteUnread != null) {
            deleteUnread.setSummary(mContext.getString(R.string.pref_delete_old_messages_unread_summary, QKPreferences.getString(QKPreference.AUTO_DELETE_UNREAD)));
        }

        EditTextPreference deleteRead = (EditTextPreference) findPreference(QKPreference.AUTO_DELETE_READ.getKey());
        if (deleteRead != null) {
            deleteRead.setSummary(mContext.getString(R.string.pref_delete_old_messages_read_summary, QKPreferences.getString(QKPreference.AUTO_DELETE_READ)));
        }

        Preference dayStart = findPreference(QKPreference.AUTO_NIGHT_DAY_START.getKey());
        if (dayStart != null) {
            dayStart.setSummary(DateFormatter.getSummaryTimestamp(mContext, QKPreferences.getString(QKPreference.AUTO_NIGHT_DAY_START)));
        }

        Preference nightStart = findPreference(QKPreference.AUTO_NIGHT_NIGHT_START.getKey());
        if (nightStart != null) {
            nightStart.setSummary(DateFormatter.getSummaryTimestamp(mContext, QKPreferences.getString(QKPreference.AUTO_NIGHT_NIGHT_START)));
        }

        EditTextPreference mmscUrl = (EditTextPreference) findPreference(QKPreference.MMSC.getKey());
        if (mmscUrl != null) {
            mmscUrl.setSummary(mmscUrl.getText());
        }

        EditTextPreference mmsProxy = (EditTextPreference) findPreference(QKPreference.MMS_PROXY.getKey());
        if (mmsProxy != null) {
            mmsProxy.setSummary(mmsProxy.getText());
        }

        EditTextPreference mmsPort = (EditTextPreference) findPreference(QKPreference.MMS_PORT.getKey());
        if (mmsPort != null) {
            mmsPort.setSummary(mmsPort.getText());
        }

        mMaxMmsAttachmentSize = (ListPreference) findPreference(QKPreference.MAX_MMS_SIZE.getKey());
        if (mMaxMmsAttachmentSize != null) {
            mMaxMmsAttachmentSizes = res.getStringArray(R.array.max_mms_attachment_sizes);

            String value = mMaxMmsAttachmentSize.getValue();
            String summary = mMaxMmsAttachmentSizes[mMaxMmsAttachmentSize.findIndexOfValue(value)];
            mMaxMmsAttachmentSize.setSummary(summary);
        }

        Preference version = findPreference(QKPreference.VERSION.getKey());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        if (QKPreference.get(key) == NOTIFICATIONS_LED_COLOR) {
            // Format the color as a nice string if it's the LED color.
            valueString = ThemeManager.getColorString(Integer.parseInt(valueString));
        }

        Log.d(TAG, "onPreferenceChange key:" + key + " newValue: " + valueString);
        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_PREFERENCE_CHANGE,
                key,
                valueString
        );

        switch (QKPreference.get(key)) {
            case BACKGROUND:
                ThemeManager.setTheme(ThemeManager.Theme.fromString((String) newValue));
                break;

            case TINTED_STATUS:
                ThemeManager.setStatusBarTintEnabled(mContext, (Boolean) newValue);
                break;

            case TINTED_NAV:
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

            case BUBBLES_COLOR_SENT:
                ThemeManager.setSentBubbleColored((Boolean) newValue);
                break;

            case BUBBLES_COLOR_RECEIVED:
                ThemeManager.setReceivedBubbleColored((Boolean) newValue);
                break;

            case AUTO_NIGHT:
                updateAlarmManager(mContext, (Boolean) newValue);
                break;

            case AUTO_NIGHT_DAY_START:
            case AUTO_NIGHT_NIGHT_START:
                updateAlarmManager(mContext, true);
                break;

            case AUTO_DELETE:
                if ((Boolean) newValue) {
                    new QKDialog()
                            .setContext(mContext)
                            .setTitle(R.string.pref_delete_old_messages)
                            .setMessage(R.string.dialog_delete_old_messages)
                            .setPositiveButton(R.string.yes, v -> {
                                QKPreferences.putBoolean(QKPreference.AUTO_DELETE, true);
                                ((CheckBoxPreference) preference).setChecked(true);
                                DeleteOldMessagesService.setupAutoDeleteAlarm(mContext);
                                mContext.makeToast(R.string.toast_deleting_old_messages);
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    return false;
                }
                break;

            case AUTO_DELETE_UNREAD:
                preference.setSummary(mContext.getString(R.string.pref_delete_old_messages_unread_summary, newValue));
                break;

            case AUTO_DELETE_READ:
                preference.setSummary(mContext.getString(R.string.pref_delete_old_messages_read_summary, newValue));
                break;

            case YAPPY_INTEGRATION:
                if ((Boolean) newValue) {
                    try {
                        EndlessJabberInterface.EnableIntegration(mContext, YappyImplementation.class, true, false, true, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!EndlessJabberInterface.IsInstalled(mContext)) {
                        EndlessJabberInterface.OpenGooglePlayLink(mContext, "QKSMS");
                    }
                } else {
                    try {
                        EndlessJabberInterface.DisableIntegration(mContext, YappyImplementation.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case QK_COMPOSE:
                NotificationManager.initQuickCompose(getActivity(), (Boolean) newValue, !(Boolean) newValue);
                break;

            case MMSC:
            case MMS_PROXY:
            case MMS_PORT:
                preference.setSummary(newValue.toString());
                break;

            case MAX_MMS_SIZE:
                // Update the summary in the list preference
                ListPreference listpref = (ListPreference) preference;
                int index = listpref.findIndexOfValue((String) newValue);
                preference.setSummary(mMaxMmsAttachmentSizes[index]);
                // Update the SMS helper static class with the new option
                SmsHelper.setMaxAttachmentSizeSetting((String) newValue);
                break;

            case DELAYED_DURATION:
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
        Intent intent = new Intent(mContext, SettingsActivity.class);
        switch (key) {
            case CATEGORY_APPEARANCE:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_appearance);
                break;

            case CATEGORY_GENERAL:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_general);
                break;

            case CATEGORY_NOTIFICATIONS:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_notifications);
                break;

            case CATEGORY_MMS:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_mms);
                break;

            case CATEGORY_QUICKREPLY:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_quickreply);
                break;

            case CATEGORY_QUICKCOMPOSE:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_quickcompose);
                break;

            case CATEGORY_ABOUT:
                intent.putExtra(SettingsActivity.ARG_SETTINGS_PAGE, R.xml.settings_about);
                break;
        }
        if (intent.hasExtra(SettingsActivity.ARG_SETTINGS_PAGE)) {
            startActivity(intent);
        }

        switch (QKPreference.get(key)) {
            case THEME:
                ThemeManager.showColorPickerDialog(mContext);
                break;

            case ICON:
                ThemeManager.setIcon(mContext);
                break;

            case BUBBLES:
                new BubblePreferenceDialog().setContext(mContext).show();
                break;

            case BLOCKED_FUTURE:
                BlockedNumberDialog.showDialog(mContext);
                break;

            case SHOULD_I_ANSWER:
                final String packageName = "org.mistergroup.muzutozvednout";
                if (!PackageUtils.isAppInstalled(mContext, packageName)) {
                    String referrer = "referrer=utm_source%3Dqksms%26utm_medium%3Dapp%26utm_campaign%3Dqksmssettings";
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
                        QKPreferences.putBoolean(QKPreference.SHOULD_I_ANSWER, false);
                        ((CheckBoxPreference) preference).setChecked(false);
                    }, 500);
                }
                break;

            case NOTIFICATIONS_LED_COLOR:
                mLedColorPickerDialog.show(getActivity().getFragmentManager(), "colorpicker");
                break;

            case AUTO_NIGHT_DAY_START:
            case AUTO_NIGHT_NIGHT_START:
                TimePickerFragment fragment = new TimePickerFragment();
                fragment.setPreference(preference);
                fragment.setOnPreferenceChangeListener(this);
                fragment.show(getFragmentManager(), "timepicker");
                break;

            case QK_RESPONSES:
                showQkResponseEditor();
                break;

            case AUTO_CONFIGURE_MMS:
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
                                (parent, view, position, id) -> {
                                    String baseUrl = ((QKTextView) view.findViewById(R.id.list_item_subtitle)).getText().toString();
                                    startBrowserIntent("https://" + baseUrl);
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

    public void showQkResponseEditor() {

        Set<String> defaultResponses = new HashSet<>(Arrays.asList(mContext.getResources().getStringArray(R.array.qk_responses)));
        Set<String> responseSet = QKPreferences.getStringSet(QKPreference.QK_RESPONSES);
        responseSet = responseSet.size() == 0 ? defaultResponses : responseSet;
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
                .setPositiveButton(R.string.save, v -> {
                    QKPreferences.putStringSet(QKPreference.QK_RESPONSES, new HashSet<>(adapter.getResponses()));
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
            calendar.setTime(simpleDateFormat.parse(QKPreferences.getString(AUTO_NIGHT_DAY_START)));
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
            calendar.setTime(simpleDateFormat.parse(QKPreferences.getString(AUTO_NIGHT_NIGHT_START)));
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
}
