package com.moez.QKSMS.ui.dialog.mms;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.moez.QKSMS.R;
import com.moez.QKSMS.mmssms.Apn;
import com.moez.QKSMS.mmssms.ApnUtils;
import com.moez.QKSMS.mmssms.Settings;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.base.QKFragment;

import java.util.List;

import static com.moez.QKSMS.ui.dialog.mms.MMSDialogFragment.DISMISS_RESULT;
import static com.moez.QKSMS.ui.dialog.mms.MMSDialogFragment.DialogFragmentListener;
import static com.moez.QKSMS.ui.dialog.mms.MMSDialogFragment.LIST_ITEM_CLICK_RESULT;
import static com.moez.QKSMS.ui.dialog.mms.MMSDialogFragment.NEGATIVE_BUTTON_RESULT;
import static com.moez.QKSMS.ui.dialog.mms.MMSDialogFragment.NEUTRAL_BUTTON_RESULT;
import static com.moez.QKSMS.ui.dialog.mms.MMSDialogFragment.POSITIVE_BUTTON_RESULT;

/**
 * @author Shane Creighton-Young
 * @since 2015-02-08
 */
public class MMSSetupFragment extends QKFragment implements DialogFragmentListener {
    public static final String TAG = "MMSSetupFragment";
    private static final boolean LOCAL_LOGV = true;

    public static final String SET_UP_MMS = "set_up_mms";
    public static final String NO_CONFIGURATIONS_FOUND = "no_configurations_found";
    public static final String ONE_CONFIGURATION_FOUND = "one_configuration_found";
    public static final String MULTIPLE_CONFIGURATIONS_FOUND = "multiple_configurations_found";
    public static final String SUCCESS = "success";
    public static final String NEXT_STEPS = "next_steps";
    public static final String SETTING_UP_MMS_LATER = "setting_up_mms_later";

    /**
     * Key for savedInstanceState to restore the dialogs on rotation.
     */
    public static final String STATE_DIALOG_TAG = "dialogTag";
    private String mDialogTag = SET_UP_MMS;

    /**
     * If true, the user will see a dialog asking them if they want to automatically configure MMS.
     * Defaults to true.
     */
    public static final String ARG_ASK_FIRST = "argAskFirst";
    private static final boolean ARG_ASK_FIRST_DEFAULT = true;

    /**
     * If non-null, the dialog will not be shown if the pref given is "true".
     *
     * Additionally, if ARG_ASK_FIRST is true, a DON'T ASK AGAIN button will be shown when asking
     * the user if they want to configure MMS.
     */
    public static final String ARG_DONT_ASK_AGAIN_PREF = "dontAskAgainPref";
    private static final String ARG_DONT_ASK_AGAIN_PREF_DEFAULT = null;

    /**
     * Contains the APNs from the last time the `query` was called.
     */
    List<Apn> mAPNs;

    // Arguments
    private boolean mArgAskFirst = ARG_ASK_FIRST_DEFAULT;
    private String mArgDontAskAgainPref = ARG_DONT_ASK_AGAIN_PREF_DEFAULT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the APNs, which will be used in MMS setup.
        mAPNs = ApnUtils.query(mContext);

        // Initialize arguments
        initArguments();

        if (savedInstanceState != null) {
            // Restore the fragment (i.e. on rotation)
            launchDialog(savedInstanceState.getString(STATE_DIALOG_TAG));

        } else {
            // This is the first time onCreate is being called---set up from arguments
            if (mArgAskFirst) {
                // Ask them if they want to configure MMS now.
                launchDialog(SET_UP_MMS);
            } else {
                // Show a dialog corresponding to the number of configurations that were found.
                showConfigurationDialog();
            }

        }
    }

    /**
     * Loads the arguments into member variables. The member variables should not be edited other
     * than in the method.
     */
    private void initArguments() {
        Bundle args = getArguments();
        if (args != null) {
            mArgAskFirst = args.getBoolean(ARG_ASK_FIRST, ARG_ASK_FIRST_DEFAULT);
            mArgDontAskAgainPref = args.getString(
                    ARG_DONT_ASK_AGAIN_PREF, ARG_DONT_ASK_AGAIN_PREF_DEFAULT
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_DIALOG_TAG, mDialogTag);
    }

    @Override
    public void onDialogFragmentResult(final int resultCode, DialogFragment fragment) {
        final String dialogTag = fragment.getTag();
        if (LOCAL_LOGV) Log.v(TAG, "onDialogFragmentResult result:" + resultCode
                + " tag:" + dialogTag);

        // Dismiss the fragment that we got the result from. We will always want to show a new
        // dialog here, rather than from the dialog fragment that we started.
        fragment.dismiss();

        // Special case: close the mms setup fragment if the user purposefully dismissed us.
        if (resultCode == DISMISS_RESULT) {
            close();
        }

        // Otherwise, handle the fragment result.

        if (SET_UP_MMS.equals(dialogTag)) {
            if (resultCode == POSITIVE_BUTTON_RESULT) {
                // Show the dialog corresponding to the number of configurations that were found.
                showConfigurationDialog();

            } else if (resultCode == NEGATIVE_BUTTON_RESULT) {
                if (mArgDontAskAgainPref != null) {
                    // Save a "don't ask again" pref (this is the DON'T ASK AGAIN button).
                    mContext.getPrefs()
                            .edit()
                            .putBoolean(mArgDontAskAgainPref, true)
                            .commit();
                }

                // Show instructions for them to set it up later.
                launchDialog(SETTING_UP_MMS_LATER);

            } else if (resultCode == NEUTRAL_BUTTON_RESULT) {
                // Close the fragment so that the dialog is shown the next time the app is opened.
                close();
            }

        } else if (NO_CONFIGURATIONS_FOUND.equals(dialogTag)) {

            if (mArgDontAskAgainPref != null) {
                // Don't annoy them with automatic configuration after we've tried and failed.
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .edit()
                        .putBoolean(mArgDontAskAgainPref, true)
                        .commit();
            }

            if (resultCode == POSITIVE_BUTTON_RESULT) {

                // Send an email to the qksms team
                contactSupport(mContext);

            } else if (resultCode == NEGATIVE_BUTTON_RESULT) {
                // Show "Next steps" dialog telling them how to set up MMS in the future.
                launchDialog(NEXT_STEPS);
            }

        // Configuration dialog that the single MMS configuration found worked well
        } else if (ONE_CONFIGURATION_FOUND.equals(dialogTag)) {
            if (resultCode == POSITIVE_BUTTON_RESULT) {
                // Success! Save the APN settings and show them instructions for how to change
                // settings later.
                Apn apn = mAPNs.get(0);
                ApnUtils.persistApn(mContext, apn);

                launchDialog(SUCCESS);

            } else if (resultCode == NEGATIVE_BUTTON_RESULT) {

                // Show the "no configurations found" dialog. This will give them the option to
                // contact support.
                launchDialog(NO_CONFIGURATIONS_FOUND);
            }

        // Multiple configurations were found and the user said that none of them looked right.
        } else if (MULTIPLE_CONFIGURATIONS_FOUND.equals(dialogTag)) {
            if (resultCode == NEGATIVE_BUTTON_RESULT) {

                // Show the "no configurations found" dialog. This will give them the option to
                // contact support.
                launchDialog(NO_CONFIGURATIONS_FOUND);
            }
        }
    }

    private void showConfigurationDialog() {
        if (mAPNs.size() == 0) {
            // Give the user a support channel and further instructions if automatic
            // configuration failed.
            launchDialog(NO_CONFIGURATIONS_FOUND);

        } else if (mAPNs.size() == 1) {
            // Ask the user for confirmation that the single APN found sounds correct.
            launchDialog(ONE_CONFIGURATION_FOUND);

        } else {
            // We'll show the user all the APN names, as well as an "N/A" option in case
            // they all look wrong.
            launchDialog(MULTIPLE_CONFIGURATIONS_FOUND);
        }
    }

    /**
     * Sends an email to mms-support@qklabs.com with a bunch of MMS-related debugging information.
     * @param context current context
     */
    public static void contactSupport(Context context) {
        if (context != null) {
            Intent intent = new Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", "mms-support@qklabs.com", null)
            );
            intent.putExtra(Intent.EXTRA_EMAIL, "mms-support@qklabs.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "MMS Support Request");
            intent.putExtra(Intent.EXTRA_TEXT, getSupportEmailBody(context));
            context.startActivity(intent);
        }
    }

    @Override
    public void onDialogFragmentListResult(int resultCode, DialogFragment fragment, int index) {
        String dialogTag = fragment.getTag();
        if (LOCAL_LOGV) Log.v(TAG, "onDialogFragmentListResult result:" + resultCode
                + " tag:" + dialogTag);

        // Dismiss the fragment that we got the result from. We will always want to show a new
        // dialog here, rather than from the dialog fragment that we started.
        fragment.dismiss();

        if (MULTIPLE_CONFIGURATIONS_FOUND.equals(dialogTag)) {
            if (resultCode == LIST_ITEM_CLICK_RESULT) {
                Apn apn = mAPNs.get(index);
                ApnUtils.persistApn(mContext, apn);

                launchDialog(SUCCESS);
            }
        }
    }

    private void close() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    private void launchDialog(final String dialogTag) {
        if (LOCAL_LOGV) Log.v(TAG, "launchDialog: " + dialogTag);
        // Save the dialog tag so that we can save it in onSaveInstanceState().
        mDialogTag = dialogTag;

        if (SET_UP_MMS.equals(dialogTag)) {
            MMSDialogFragment f = new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.set_up_mms_title)
                    .setMessage(R.string.set_up_mms_description)
                    .setListener(this)
                    .setPositiveButton(R.string.okay)
                    .setNeutralButton(R.string.later);

            // Show a don't ask again button if there is a don't ask again pref string in the
            // arguments
            if (mArgDontAskAgainPref != null) {
                f.setNegativeButton(R.string.dont_ask_again);
            }

            // Show the dialog
            f.show(getFragmentManager(), SET_UP_MMS);

        } else if (NO_CONFIGURATIONS_FOUND.equals(dialogTag)) {
            new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.mms_setup_no_configurations_found_title)
                    .setMessage(R.string.mms_setup_no_configurations_found_body)
                    .setListener(this)
                    .setPositiveButton(R.string.okay)
                    .setNegativeButton(R.string.cancel)
                    .show(getFragmentManager(), NO_CONFIGURATIONS_FOUND);

        } else if (ONE_CONFIGURATION_FOUND.equals(dialogTag)) {
            String carrier = mAPNs.get(0).name;
            String message = getString(R.string.mms_setup_one_configuration_found_body,
                    carrier);

            new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.mms_setup_one_configuration_found_title)
                    .setMessage(message)
                    .setListener(this)
                    .setPositiveButton(R.string.yes)
                    .setNegativeButton(R.string.no)
                    .show(getFragmentManager(), ONE_CONFIGURATION_FOUND);

        } else if (MULTIPLE_CONFIGURATIONS_FOUND.equals(dialogTag)) {
            String[] items = new String[mAPNs.size()];
            for (int i = 0; i < mAPNs.size(); i++) {
                items[i] = mAPNs.get(i).name;
            }

            new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.mms_setup_multiple_configurations_found_title)
                    .setMessage(R.string.mms_setup_multiple_configurations_found_body)
                    .setListener(this)
                    .setNegativeButton(R.string.na)
                    .setItems(items)
                    .show(getFragmentManager(), MULTIPLE_CONFIGURATIONS_FOUND);

        } else if (SETTING_UP_MMS_LATER.equals(dialogTag)) {
            new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.mms_setup_setting_up_later_title)
                    .setMessage(R.string.mms_setup_setting_up_later_body)
                    .setListener(this)
                    .setPositiveButton(R.string.okay)
                    .show(getFragmentManager(), SETTING_UP_MMS_LATER);

        } else if (NEXT_STEPS.equals(dialogTag)) {
            new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.mms_setup_next_steps_title)
                    .setMessage(R.string.mms_setup_next_steps_body)
                    .setListener(this)
                    .setPositiveButton(R.string.okay)
                    .show(getFragmentManager(), NEXT_STEPS);

        } else if (SUCCESS.equals(dialogTag)) {
            new MMSDialogFragment()
                    .setContext(mContext)
                    .setTitle(R.string.mms_setup_success_title)
                    .setMessage(R.string.mms_setup_success_body)
                    .setListener(this)
                    .setPositiveButton(R.string.okay)
                    .show(getFragmentManager(), SUCCESS);

        }
    }

    private static String getSupportEmailBody(Context context) {
        if (context != null) {
            TelephonyManager manager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            Settings settings = SmsHelper.getSendSettings(context);

            // Build the message body
            StringBuilder body = new StringBuilder();
            body.append("Press send, and the QKSMS team will find the correct MMS settings for you!\n\n");
            body.append("------------- DO NOT MODIFY -------------\n");
            body.append("Data activity: ").append(manager.getDataActivity()).append("\n");
            if (Build.VERSION.SDK_INT >= 19) {
                body.append("MMS UAProfUrl: ").append(manager.getMmsUAProfUrl()).append("\n");
                body.append("MMS User Agent: ").append(manager.getMmsUserAgent()).append("\n");
            }
            body.append("Network operator: ").append(manager.getNetworkOperator()).append("\n");
            body.append("Network name: ").append(manager.getNetworkOperatorName()).append("\n");
            body.append("Radio type: ").append(manager.getPhoneType()).append("\n");
            body.append("Sim operator: ").append(manager.getSimOperator()).append("\n");
            body.append("Sim operator name: ").append(manager.getSimOperatorName()).append("\n");
            body.append("Subscriber ID: ").append(manager.getSubscriberId()).append("\n");
            body.append("\n");
            body.append("Automatically configured APNs:\n");

            List<Apn> apns = ApnUtils.query(context);
            if (apns != null) {
                for (Apn apn : apns) {
                    body.append(apn.toString()).append("\n");
                }
            }
            body.append("\n");
            body.append("Selected APN settings:\n");
            body.append(String.format("{name:%s, mmsc:%s, proxy:%s, port:%s}",
                    settings.getUaProfTagName(),
                    settings.getMmsc(),
                    settings.getProxy(),
                    settings.getPort()));

            return body.toString();
        } else {
            return null;
        }
    }
}
