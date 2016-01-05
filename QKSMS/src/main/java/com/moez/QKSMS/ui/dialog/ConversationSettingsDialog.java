package com.moez.QKSMS.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.QKPreference;
import com.moez.QKSMS.ui.view.QKRingtonePreference;
import com.moez.QKSMS.ui.view.QKSwitchPreference;
import com.moez.QKSMS.ui.view.QKTextView;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerDialog;

public class ConversationSettingsDialog extends QKDialog implements Preference.OnPreferenceClickListener {
    private final String TAG = "ConversationSettingsDialog";

    public static final int RINGTONE_REQUEST_CODE = 716;
    public static final String ARG_THREAD_ID = "thread_id";
    private static final String ARG_NAME = "name";

    private Resources mRes;
    private ConversationPrefsHelper mConversationPrefs;

    private int[] mLedColors;

    private long mThreadId;
    private ViewGroup.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    public static ConversationSettingsDialog newInstance(long threadId, String name) {
        ConversationSettingsDialog dialog = new ConversationSettingsDialog();

        Bundle bundle = new Bundle();
        bundle.putLong(ARG_THREAD_ID, threadId);
        bundle.putString(ARG_NAME, name);

        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setTitle(getArguments().getString(ARG_NAME));
        mThreadId = getArguments().getLong(ARG_THREAD_ID);

        mRes = getActivity().getResources();
        mConversationPrefs = new ConversationPrefsHelper(getActivity(), mThreadId);

        mLedColors = new int[]{
                mRes.getColor(R.color.blue_light), mRes.getColor(R.color.purple_light),
                mRes.getColor(R.color.green_light), mRes.getColor(R.color.yellow_light),
                mRes.getColor(R.color.red_light), mRes.getColor(R.color.white_pure)
        };

        int padding = Units.dpToPx(getActivity(), 16);
        QKTextView premiumWarning = new QKTextView(getActivity());
        premiumWarning.setLayoutParams(mLayoutParams);
        premiumWarning.setPadding(padding, padding, padding, padding);

        LinearLayout list = new LinearLayout(getActivity());
        list.setOrientation(LinearLayout.VERTICAL);

        list.addView(new QKPreference(getActivity(), this, SettingsFragment.THEME,
                R.string.pref_theme, R.string.pref_theme_summary_alt).getView());

        list.addView(new QKSwitchPreference(getActivity(), this, SettingsFragment.NOTIFICATION_LED,
                mConversationPrefs.getConversationPrefs(), mConversationPrefs.getNotificationLedEnabled(), R.string.pref_led, 0).getView());

        list.addView(new QKPreference(getActivity(), this, SettingsFragment.NOTIFICATION_LED_COLOR,
                R.string.pref_theme_led, 0).getView());

        list.addView(new QKSwitchPreference(getActivity(), this, SettingsFragment.WAKE,
                mConversationPrefs.getConversationPrefs(), mConversationPrefs.getWakePhoneEnabled(), R.string.pref_wake, R.string.pref_wake_summary).getView());

        list.addView(new QKSwitchPreference(getActivity(), this, SettingsFragment.NOTIFICATION_TICKER,
                mConversationPrefs.getConversationPrefs(), mConversationPrefs.getTickerEnabled(), R.string.pref_ticker, R.string.pref_ticker_summary).getView());

        list.addView(new QKSwitchPreference(getActivity(), this, SettingsFragment.NOTIFICATION_VIBRATE,
                mConversationPrefs.getConversationPrefs(), mConversationPrefs.getVibrateEnabled(), R.string.pref_vibration, R.string.pref_vibration_summary).getView());

        list.addView(new QKRingtonePreference(getActivity(), this, SettingsFragment.NOTIFICATION_TONE,
                R.string.pref_ringtone, R.string.pref_ringtone_summary).getView());

        list.addView(new QKSwitchPreference(getActivity(), this, SettingsFragment.NOTIFICATION_CALL_BUTTON,
                mConversationPrefs.getConversationPrefs(), mConversationPrefs.getCallButtonEnabled(), R.string.pref_notification_call, R.string.pref_notification_call_summary).getView());

        setCustomView(list);

        return super.onCreateDialog(savedInstanceState);
    }

    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case SettingsFragment.THEME:
                ThemeManager.showColorPickerDialogForConversation(mContext, mConversationPrefs);
                break;

            case SettingsFragment.NOTIFICATION_LED_COLOR:
                ColorPickerDialog ledColorPickerDialog = new ColorPickerDialog();
                ledColorPickerDialog.initialize(R.string.pref_theme_led, mLedColors, Integer.parseInt(mConversationPrefs.getNotificationLedColor()), 3, 2);
                ledColorPickerDialog.setOnColorSelectedListener(color -> mConversationPrefs.putString(SettingsFragment.NOTIFICATION_LED_COLOR, "" + color));
                ledColorPickerDialog.show(getActivity().getFragmentManager(), "colorpicker");
                break;

            case SettingsFragment.NOTIFICATION_TONE:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                Uri uri = Uri.parse(mConversationPrefs.getString(SettingsFragment.NOTIFICATION_TONE, "content://settings/system/notification_sound"));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.pref_ringtone));
                intent.putExtra(ARG_THREAD_ID, mThreadId);
                ((MainActivity) getActivity()).getResultForThreadId(mThreadId);
                getActivity().startActivityForResult(intent, RINGTONE_REQUEST_CODE);
                break;
        }

        return true;
    }

}
