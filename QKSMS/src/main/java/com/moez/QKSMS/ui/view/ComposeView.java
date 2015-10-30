package com.moez.QKSMS.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.common.io.ByteStreams;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.AnalyticsManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.utils.ImageUtils;
import com.moez.QKSMS.common.utils.PhoneNumberUtils;
import com.moez.QKSMS.common.utils.RecordAudioUtils;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.interfaces.ActivityLauncher;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.interfaces.RecipientProvider;
import com.moez.QKSMS.mmssms.Transaction;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.dialog.mms.MMSSetupFragment;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComposeView extends LinearLayout implements View.OnClickListener, LiveView {
    public final static String TAG = "ComposeView";

    private final String KEY_DELAYED_INFO_DIALOG_SHOWN = "delayed_info_dialog_shown";

    private final int ANIMATION_DURATION = 300;

    public interface OnSendListener {
        void onSend(String[] addresses, String body);
    }

    enum SendButtonState {
        SEND, // send a messaage
        ATTACH, // open the attachment panel
        RECORD, // is recording a voice note
        CLOSE, // close the attachment panel
        CANCEL // cancel a message while it's sending
    }

    private QKActivity mContext;
    private SharedPreferences mPrefs;
    private Resources mRes;

    private Conversation mConversation;
    private ConversationLegacy mConversationLegacy;

    private ActivityLauncher mActivityLauncher;
    private OnSendListener mOnSendListener;
    private RecipientProvider mRecipientProvider;

    // Analytics
    // This string is sent along to events that happen in ComposeView, so that we know where they're
    // happening (i.e. QKReply, QKCompose, etc)
    private String mLabel;

    // Views
    private QKEditText mReplyText;
    private FrameLayout mButton;
    private DonutProgress mProgress;
    private ImageView mButtonBackground;
    private ImageView mButtonBar1;
    private ImageView mButtonBar2;
    private ImageView mButtonVoice;
    private ImageButton mAttach;
    private ImageButton mCamera;
    private ImageButton mDelay;
    private ImageButton mAudio;
    private View mAttachmentPanel;
    private QKTextView mLetterCount;
    private FrameLayout mAttachmentLayout;
    private AttachmentImageView mAttachment;
    private ImageButton mCancel;

    // State
    private boolean mDelayedMessagingEnabled;
    private boolean mSendingCancelled;
    private boolean mIsSendingBlocked;
    private boolean mIsRecording;
    private String mSendingBlockedMessage;

    private String mCurrentPhotoPath;
    private ValueAnimator mProgressAnimator;
    private int mDelayDuration = 3000;
    private int mNoteVibrationTime = 30;

    private byte[] audioAttachment;

    private SendButtonState mButtonState = SendButtonState.ATTACH;

    private static final int REQUEST_CODE_IMAGE = 0x00F1;
    private static final int REQUEST_CODE_CAMERA = 0x00F2;
    private static final int REQUEST_CODE_AUDIO = 0x00F3;

    private Vibrator vi = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

    public ComposeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ComposeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = (QKActivity) context;
        mPrefs = mContext.getPrefs();
        mRes = mContext.getResources();

        mDelayedMessagingEnabled = mPrefs.getBoolean(SettingsFragment.DELAYED, false);
        try {
            mDelayDuration = Integer.parseInt(mPrefs.getString(SettingsFragment.DELAY_DURATION, "3"));
            if (mDelayDuration < 1) {
                mDelayDuration = 1;
            } else if (mDelayDuration > 30) {
                mDelayDuration = 30;
            }
            mDelayDuration *= 1000;
        } catch (Exception e) {
            mDelayDuration = 3000;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        // Get references to the views
        mReplyText = (QKEditText) findViewById(R.id.compose_reply_text);
        mButton = (FrameLayout) findViewById(R.id.compose_button);
        mProgress = (DonutProgress) findViewById(R.id.progress);
        mButtonBackground = (ImageView) findViewById(R.id.compose_button_background);
        mButtonBar1 = (ImageView) findViewById(R.id.compose_button_bar_1);
        mButtonBar2 = (ImageView) findViewById(R.id.compose_button_bar_2);
        mButtonVoice = (ImageView) findViewById(R.id.compose_button_voice);
        mAttachmentPanel = findViewById(R.id.attachment_panel);
        mAttach = (ImageButton) findViewById(R.id.attach);
        mCamera = (ImageButton) findViewById(R.id.camera);
        mDelay = (ImageButton) findViewById(R.id.delay);
        mAudio = (ImageButton) findViewById(R.id.audio);
        mLetterCount = (QKTextView) findViewById(R.id.compose_letter_count);
        mAttachmentLayout = (FrameLayout) findViewById(R.id.attachment);
        mAttachment = (AttachmentImageView) findViewById(R.id.compose_attachment);
        mCancel = (ImageButton) findViewById(R.id.cancel);

        mAttach.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mDelay.setOnClickListener(this);
        mAudio.setOnClickListener(this);

        mButton.setOnTouchListener(new OnTouchListener() { //Set OnTouchListener to differentiate between long & short click
            private RecordAudioUtils recordAudioUtils = new RecordAudioUtils();
            private long then;
            private int longClickTime = 260; // set time for a long click in ms
            private int minRecordTime = 400; // set min record time
            private Rect rect;
            private boolean outside = false;
            private final Handler handler = new Handler();
            private Runnable mLongPressed = new Runnable() {
                public void run() { //Start record
                    handleAudioRecord(0, recordAudioUtils);
                }
            };
            private Runnable mRecordCanceled = new Runnable() {
                public void run() { //cancel record
                    handleAudioRecord(1, recordAudioUtils);
                }
            };
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        then = System.currentTimeMillis();
                        //Start delayed runnable in background which will start the record when it isn't canceled in time
                        if (mButtonState == SendButtonState.ATTACH) handler.postDelayed(mLongPressed, longClickTime);
                        // Construct a rect of the view's bounds
                        rect = new Rect(v.getLeft() + 600,
                                v.getTop() + 50,
                                v.getRight() + 50,
                                v.getBottom() + 50);
                        return true;

                    case MotionEvent.ACTION_UP:
                        long elapsedTime = (System.currentTimeMillis() - then);

                        if (elapsedTime < longClickTime || mButtonState == SendButtonState.SEND ||
                                mButtonState == SendButtonState.CLOSE) { //Short click
                            handleComposeButtonClick(); //handle normal compose button click
                            handler.removeCallbacks(mLongPressed); //stop runnable who would otherwise start the record
                        }
                        else if (elapsedTime < (longClickTime + minRecordTime) || outside) { //Outside of rect or too short
                            vi.vibrate(mNoteVibrationTime);
                            //Prevent crash when record isn't completely started
                            handler.postDelayed(mRecordCanceled, 400); //stop & delete record
                        }
                        else handleAudioRecord(2, recordAudioUtils); //record stopped, send record
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        /* Reference http://stackoverflow.com/a/26423204/4026792
                           Check if moved out of rect */
                        v.getHitRect(rect);
                        outside = !rect.contains(Math.round(v.getX() + event.getX()), Math.round(v.getY() + event.getY()));
                        return true;
                }
                return false;
            }
        });

        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);
        LiveViewManager.registerPreference(this, SettingsFragment.BACKGROUND);

        refresh();

        // There is an option for using the return button instead of the emoticon button in the
        // keyboard; set that up here.
        switch (Integer.parseInt(mPrefs.getString(SettingsFragment.ENTER_BUTTON, "0"))) {
            case 0: // emoji
                break;
            case 1: // new line
                mReplyText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                        InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
                mReplyText.setSingleLine(false);
                break;
            case 2: // send
                mReplyText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                mReplyText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                mReplyText.setSingleLine(false);
                mReplyText.setOnKeyListener(new OnKeyListener() { //Workaround because ACTION_SEND does not support multiline mode
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66) {
                            sendSms();
                            return true;
                        }
                        return false;
                    }});
                break;
        }

        mReplyText.setTextChangedListener(new QKEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s) {
                int length = s.length();

                updateButtonState(length);

                // If the reply is within 10 characters of the SMS limit (160), it will start counting down
                // If the reply exceeds the SMS limit, it will count down until an extra message will have to be sent, and shows how many messages will currently be sent
                if (length < 150) {
                    mLetterCount.setText("");
                } else if (150 <= length && length <= 160) {
                    mLetterCount.setText("" + (160 - length));
                } else if (160 < length) {
                    mLetterCount.setText((160 - length % 160) + "/" + (length / 160 + 1));
                }
            }
        });

        mProgressAnimator = new ValueAnimator();
        mProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mProgressAnimator.setDuration(mDelayDuration);
        mProgressAnimator.setIntValues(0, 360);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress.setProgress((int) animation.getAnimatedValue());
            }
        });
        mProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mProgress.setVisibility(INVISIBLE);
                mProgress.setProgress(0);

                if (!mSendingCancelled) {
                    sendSms();
                    // In case they only enabled it for a particular message, let's set it back to the pref value
                    mDelayedMessagingEnabled = mPrefs.getBoolean(SettingsFragment.DELAYED, false);
                    updateDelayButton();
                } else {
                    mSendingCancelled = false;
                    updateButtonState();
                }
            }
        });
    }

    /**
     * Sets the ActivityLauncher. This can be an Activity, a Fragment, or in general something that
     * implements startActivityForResult(Intent, int), and onActivityResult(int, int, Intent); this
     * instance must be able to launch and get results for activties.
     * <p/>
     * Additionally, in the onActivityResult(int, int, Intent) method, the ActivityLauncher instance
     * should pass along that result value to this ComposeFragment, using its own onActivityResult
     * method.
     *
     * @param launcher
     */
    public void setActivityLauncher(ActivityLauncher launcher) {
        mActivityLauncher = launcher;
    }

    /**
     * Sets a listener to be pinged when an SMS message is sent.
     *
     * @param l
     */
    public void setOnSendListener(OnSendListener l) {
        mOnSendListener = l;
    }

    /**
     * Sets a RecipientProvider. The RecipientProvider provides one method, getRecipientAddresses,
     * which returns a String[] of recipient addresses. This method will be called when we're trying
     * to send an SMS/MMS message, and onOpenConversation has NOT been called with a non-null
     * Conversation object, i.e. we cannot use the Conversation object to get recipient addresses.
     *
     * @param p
     */
    public void setRecipientProvider(RecipientProvider p) {
        mRecipientProvider = p;
    }

    /**
     * Handles activity results that were started by this View. Returns true if the result was
     * handled by this view, false otherwise.
     *
     * @param requestCode
     * @param resultCode
     * @param data Uri for file
     */
    public boolean onActivityResult(int requestCode, int resultCode, final Intent data) {
        boolean result = false;

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            result = true;

            Toast.makeText(mContext, R.string.compose_loading_attachment, Toast.LENGTH_LONG).show();
            new ImageLoaderTask(mContext, data.getData()).execute();
        }
        else if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            result = true;
            Toast.makeText(mContext, R.string.compose_loading_attachment, Toast.LENGTH_LONG).show();
            new ImageLoaderFromCameraTask().execute((Void[]) null);
        }
        else if (requestCode == REQUEST_CODE_AUDIO && resultCode == Activity.RESULT_OK) {
            result = true;
            InputStream inputStream;
            try {inputStream = getContext().getContentResolver().openInputStream(data.getData());}
            catch (FileNotFoundException e){return true;}
            new AudioLoaderTask(mContext, inputStream, false).execute();
            updateButtonState();
        }

        return result;
    }

    private void updateButtonState() {
        updateButtonState(mReplyText == null ? 0 : mReplyText.getText().length());
    }

    /**
     * Sets the button image based on the length of the reply text, and whether or not the drawable
     * is set.
     */
    private void updateButtonState(int length) {
        SendButtonState buttonState;

        if (mAttachmentPanel.getVisibility() == View.VISIBLE) {
            buttonState = SendButtonState.CLOSE;
        } else if (length > 0 || mAttachment.hasAttachment() || audioAttachment != null) {
            buttonState = SendButtonState.SEND;
        } else if (mIsRecording) {
            buttonState = SendButtonState.RECORD;
        } else {
            buttonState = SendButtonState.ATTACH;
        }

        updateButtonState(buttonState);
    }

    private void updateButtonState(SendButtonState buttonState) {
        if (mButtonState != buttonState) {
            mButtonState = buttonState;
            if (mIsRecording) {
                mButtonBar1.setVisibility(INVISIBLE);
                mButtonBar2.setVisibility(INVISIBLE);
                mButtonVoice.setVisibility(VISIBLE);
            }
            else {
                mButtonVoice.setVisibility(INVISIBLE);
                mButtonBar1.setVisibility(VISIBLE);
                mButtonBar2.setVisibility(VISIBLE);
                float translation = Units.dpToPx(mContext, 14) / 3;
                float barRotation1 = mButtonBar1.getRotation();
                float barTranslation1 = mButtonBar1.getTranslationY();
                float barRotation2 = mButtonBar2.getRotation();
                float barTranslation2 = mButtonBar2.getTranslationY();
                float barRotationTarget1 = mButtonState == SendButtonState.ATTACH ? 0 : 225;
                float barTranslationTarget1 = mButtonState == SendButtonState.SEND ? -translation : 0;
                float barRotationTarget2 = mButtonState == SendButtonState.ATTACH ? 90 : 135;
                float barTranslationTarget2 = mButtonState == SendButtonState.SEND ? translation : 0;

                ObjectAnimator.ofFloat(mButtonBar1, "rotation", barRotation1, barRotationTarget1)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                ObjectAnimator.ofFloat(mButtonBar2, "rotation", barRotation2, barRotationTarget2)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                ObjectAnimator.ofFloat(mButtonBar1, "translationY", barTranslation1, barTranslationTarget1)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                ObjectAnimator.ofFloat(mButtonBar2, "translationY", barTranslation2, barTranslationTarget2)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        }
    }

    /**
     * Sets the text of the Reply edit text.
     *
     * @param text
     */
    public void setText(String text) {
        mReplyText.setText(text);
        mReplyText.setSelection(mReplyText.getText().length());
    }

    public void setSendingUnblocked() {
        mSendingBlockedMessage = null;
        mIsSendingBlocked = false;
    }

    public void setSendingBlocked(String message) {
        mSendingBlockedMessage = message;
        mIsSendingBlocked = true;
    }

    /**
     * Requests focus to the Reply edit text.
     */
    public void requestReplyTextFocus() {
        mReplyText.requestFocus();
    }

    public void sendDelayedSms() {
        mProgress.setVisibility(VISIBLE);
        updateButtonState(SendButtonState.CANCEL);
        mProgressAnimator.start();
    }
    public void sendSms() {
        sendSms(null);
    }

    public void sendSms(byte[] audio) {
        String body = mReplyText.getText().toString();

        final Drawable attachment;
        if (mAttachment.hasAttachment()) {
            attachment = mAttachment.getDrawable();
        } else {
            attachment = null;
        }
        clearAttachment();

        String[] recipients = null;
        if (mConversation != null) {
            recipients = mConversation.getRecipients().getNumbers();
            for (int i = 0; i < recipients.length; i++) {
                recipients[i] = PhoneNumberUtils.stripSeparators(recipients[i]);
            }
        } else if (mRecipientProvider != null) {
            recipients = mRecipientProvider.getRecipientAddresses();
        }

        // If we have some recipients, send the message!
        if (recipients != null && recipients.length > 0) {
            mReplyText.setText("");

            AnalyticsManager.getInstance().sendEvent(
                    AnalyticsManager.CATEGORY_MESSAGES,
                    AnalyticsManager.ACTION_SEND_MESSAGE,
                    mLabel
            );

            Transaction sendTransaction = new Transaction(mContext, SmsHelper.getSendSettings(mContext));

            com.moez.QKSMS.mmssms.Message message = new com.moez.QKSMS.mmssms.Message(body, recipients);
            message.setType(com.moez.QKSMS.mmssms.Message.TYPE_SMSMMS);
            if (audio != null){
                message.setAudio(audio);
            }
            else if (attachment != null) {
                message.setImage(ImageUtils.drawableToBitmap(attachment));
            }

            // Notify the listener about the new text message
            if (mOnSendListener != null) {
                mOnSendListener.onSend(recipients, message.getSubject());
            }

            long threadId = mConversation != null ? mConversation.getThreadId() : 0;
            if (!message.toString().equals("")) {
                sendTransaction.sendNewMessage(message, threadId);
            }
            NotificationManager.update(mContext);

            if (mConversationLegacy != null) {
                mConversationLegacy.markRead();
            }

            // Reset the image button state
            updateButtonState();

            // Otherwise, show a toast to the user to prompt them to add recipients.
        } else {
            Toast.makeText(
                    mContext,
                    mRes.getString(R.string.error_no_recipients),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                clearAttachment();
                break;

            case R.id.attach:
                if (hasSetupMms()) {
                    mAttachmentPanel.setVisibility(GONE);
                    updateButtonState();
                    chooseAttachmentFromGallery();
                }
                break;

            case R.id.camera:
                if (hasSetupMms()) {
                    mAttachmentPanel.setVisibility(GONE);
                    updateButtonState();
                    attachFromCamera();
                }
                break;

            case R.id.delay:
                if (!mPrefs.getBoolean(KEY_DELAYED_INFO_DIALOG_SHOWN, false) && !mDelayedMessagingEnabled) {
                    showDelayedMessagingInfo();
                } else {
                    toggleDelayedMessaging();
                }
                break;

            case R.id.audio:
                if (hasSetupMms()) {
                    mAttachmentPanel.setVisibility(GONE);
                    updateButtonState();
                    chooseAudioAttachment();
                }
                break;
        }
    }

    private void toggleDelayedMessaging() {
        mDelayedMessagingEnabled = !mDelayedMessagingEnabled;
        updateDelayButton();
        mAttachmentPanel.setVisibility(GONE);
        updateButtonState();
    }

    private void showDelayedMessagingInfo() {
        new QKDialog()
                .setContext(mContext)
                .setTitle(R.string.pref_delayed)
                .setMessage(R.string.delayed_messaging_info)
                .setNegativeButton(R.string.just_once, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleDelayedMessaging();
                    }
                })
                .setPositiveButton(R.string.enable, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPrefs.edit().putBoolean(SettingsFragment.DELAYED, true).apply();
                        toggleDelayedMessaging();
                    }
                })
                .show((mContext).getFragmentManager(), "delayed message info");
        mPrefs.edit().putBoolean(KEY_DELAYED_INFO_DIALOG_SHOWN, false).apply(); //Fixme
    }

    private void handleAudioRecord(int record, RecordAudioUtils recordAudioUtils) {
        switch(record){
            case 0: //record started
                recordAudioUtils.onRecord(true);
                mIsRecording = true;
                vi.vibrate(mNoteVibrationTime);
                //mButton.setLayoutParams(new RelativeLayout.LayoutParams(100, 100));

                break;
            case 1:  // record stopped, delete result
                mIsRecording = false;
                recordAudioUtils.onRecord(false);
                try{boolean deleted = recordAudioUtils.getCurrentFile().delete();} //workaround to delete files
                catch (NullPointerException e){Log.w(TAG, "Couldn't delete File", e);}
                break;
            case 2: //record stopped, send result
                mIsRecording = false;
                vi.vibrate(mNoteVibrationTime);
                recordAudioUtils.onRecord(false);
                FileInputStream file;
                try{file = new FileInputStream(recordAudioUtils.getCurrentFile());}
                catch (FileNotFoundException | NullPointerException e){
                    Log.e(TAG, "File not found:" + e);
                    break;
                }
                new AudioLoaderTask(mContext, file, true).execute();
                break;
            default: break;
        }
        updateButtonState();
    }


    private void handleComposeButtonClick() {

        switch (mButtonState) {
            case ATTACH:
                mAttachmentPanel.setVisibility(VISIBLE);
                updateButtonState();
                break;

            case SEND:
                // If the API version is less than KitKat, they can send an SMS; so do this.
                if (Build.VERSION.SDK_INT < 19) {
                    if (mDelayedMessagingEnabled) {
                        sendDelayedSms();
                    } else {
                        sendSms();
                    }
                } else {
                    // Otherwise... check if we're not the default SMS app
                    boolean isDefaultSmsApp = Utils.isDefaultSmsApp(mContext);

                    // Now make sure that a client hasn't blocked sending, i.e. in the welcome
                    // screen when we have a demo conversation.
                    if (mIsSendingBlocked) {
                        // Show the sending blocked message (if it exists)
                        Toast.makeText(
                                mContext,
                                mSendingBlockedMessage,
                                Toast.LENGTH_SHORT
                        ).show();

                    } else if (!isDefaultSmsApp) {
                        // Ask to become the default SMS app
                        new DefaultSmsHelper(mContext, null, R.string.not_default_send).showIfNotDefault(this);
                    }
                    else if (audioAttachment != null) {
                        if (mDelayedMessagingEnabled) sendSms(audioAttachment); //FIXME should be delayed
                        else sendSms(audioAttachment);
                        audioAttachment = null;
                    }
                    else if (!TextUtils.isEmpty(mReplyText.getText()) || mAttachment.hasAttachment()) {
                        if (mDelayedMessagingEnabled) sendDelayedSms();
                        else sendSms();
                    }
                }
                break;

            case CLOSE:
                mAttachmentPanel.setVisibility(GONE);
                updateButtonState();
                break;

            case CANCEL:
                mSendingCancelled = true;
                mProgressAnimator.end();
                //updateButtonState();
                break;
        }
    }

    private boolean hasSetupMms() {
        if (TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMSC_URL, ""))
                && TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMS_PROXY, ""))
                && TextUtils.isEmpty(mPrefs.getString(SettingsFragment.MMS_PORT, ""))) {

            // Not so fast! You need to set up MMS first.
            MMSSetupFragment f = new MMSSetupFragment();
            Bundle args = new Bundle();
            args.putBoolean(MMSSetupFragment.ARG_ASK_FIRST, true);
            f.setArguments(args);

            (mContext).getFragmentManager()
                    .beginTransaction()
                    .add(f, MMSSetupFragment.TAG)
                    .commit();

            return false;
        }

        return true;
    }

    private void attachFromCamera() {

        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_MESSAGES,
                AnalyticsManager.ACTION_ATTACH_FROM_CAMERA,
                mLabel
        );

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {

            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                mActivityLauncher.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
            }
        } else {
            // Send a toast saying there was a camera error
            if (mContext != null) {
                String message = mContext.getResources().getString(R.string.attachment_camera_error);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseAttachmentFromGallery() {

        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_MESSAGES,
                AnalyticsManager.ACTION_ATTACH_IMAGE,
                mLabel
        );

        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            mActivityLauncher.startActivityForResult(photoPickerIntent, REQUEST_CODE_IMAGE);
        } catch (ActivityNotFoundException e) {
            // Send a toast saying no picture apps
            if (mContext != null) {
                String message = mContext.getResources().getString(R.string.attachment_app_not_found);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseAudioAttachment() {

        try {
            //Intent audioPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            Intent audioPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            audioPickerIntent.setType("audio/*");
            Intent c = Intent.createChooser(audioPickerIntent, "Select soundfile");
            mActivityLauncher.startActivityForResult(c, REQUEST_CODE_AUDIO);
        } catch (ActivityNotFoundException e) {
            // Send a toast saying no picture (audio) apps FIXME new string needed
            if (mContext != null) {
                String message = mContext.getResources().getString(R.string.attachment_app_not_found);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Sets the conversation for this compose view. This will setup the ComposeView with drafts.
     *
     * @param conversationLegacy
     */
    public void onOpenConversation(Conversation conversation, ConversationLegacy conversationLegacy) {
        long threadId = mConversation != null ? mConversation.getThreadId() : -1;
        if (threadId > 0) sendPendingDelayedMessage();
        long newThreadId = conversation != null ? conversation.getThreadId() : -1;
        if (mConversation != null && mConversationLegacy != null && threadId != newThreadId) {
            // Save the old draft first before updating the conversation objects.
            saveDraft();
        }

        mConversation = conversation;
        mConversationLegacy = conversationLegacy;

        // If the conversation was different, set up the draft here.
        if (threadId != newThreadId) {
            setupDraft();
        }
    }

    /**
     * If there's a pending delayed message, end the progress animation and go ahead with sending the message
     */
    private void sendPendingDelayedMessage() {
        if (mButtonState == SendButtonState.CANCEL && mProgressAnimator != null) {
            mProgressAnimator.end();
        }
    }

    /**
     * Saves a draft to the conversation.
     */
    public void saveDraft() {
        // If the conversation_reply view is null, then we won't worry about saving drafts at all. We also don't save
        // drafts if a message is about to be sent (delayed)
        if (mReplyText != null && mConversation != null && mButtonState != SendButtonState.CANCEL) {
            String draft = mReplyText.getText().toString();

            if (mConversationLegacy.hasDraft() && TextUtils.isEmpty(draft)) {
                mConversationLegacy.clearDrafts();

            } else if (!TextUtils.isEmpty(draft) &&
                    (!mConversationLegacy.hasDraft() ||
                            !draft.equals(mConversationLegacy.getDraft()))) {
                mConversationLegacy.saveDraft(draft);
            }
        }

    }

    /**
     * Displays the draft message to the user.
     */
    private void setupDraft() {
        if (mConversationLegacy != null) {
            if (mConversationLegacy.hasDraft()) {
                String text = mConversationLegacy.getDraft();

                mReplyText.setText(text);
                mReplyText.setSelection(text != null ? text.length() : 0);
                clearAttachment();
            } else {
                // Since this view can be reused, it's important to set the text to empty when there
                // isn't a new draft. Or else the previous conversation's draft can be carried on to
                // this new conversation.
                mReplyText.setText("");
                clearAttachment();
            }
        }
    }

    /**
     * Loads message data from an intent. Currently supports text/plain and image/* ACTION_SEND
     * intents.
     *
     * @param intent The intent with the data to load.
     */
    public void loadMessageFromIntent(final Intent intent) {
        String type = intent == null ? null : intent.getType();

        if (intent != null) {

            if (type != null) {

                if ("text/plain".equals(type)) {
                    mReplyText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));

                } else if (type.startsWith("image/")) {

                    Uri uri = intent.getData();

                    // If the Uri is null, try looking elsewhere for it. [1] [2]
                    // [1]: http://stackoverflow.com/questions/10386885/intent-filter-intent-getdata-returns-null
                    // [2]: http://developer.android.com/reference/android/content/Intent.html#ACTION_SEND
                    if (uri == null) {
                        Bundle extras = intent.getExtras();
                        if (extras != null) {
                            uri = (Uri) extras.get(Intent.EXTRA_STREAM);
                        }
                    }

                    new ImageLoaderTask(mContext, uri).execute();

                    // If the Uri is still null here, throw the exception.
                    if (uri == null) {
                        // TODO show the user some kind of feedback
                    }
                }
            } else {
                if (intent.getExtras() != null) {
                    String body = intent.getExtras().getString("sms_body");
                    if (body != null) {
                        mReplyText.setText(body);
                    }
                }
            }
        }
    }

    /**
     * Clears the image from the attachment view.
     */
    public void clearAttachment() {
        mAttachment.setImageBitmap(null);
        mAttachmentLayout.setVisibility(View.GONE);
        updateButtonState();
    }

    /**
     * Sets the image of the attachment view.
     *
     * @param imageBitmap the bitmap
     */
    public void setAttachment(Bitmap imageBitmap) {
        if (imageBitmap == null) {
            clearAttachment();
        } else {
            AnalyticsManager.getInstance().sendEvent(
                    AnalyticsManager.CATEGORY_MESSAGES,
                    AnalyticsManager.ACTION_ATTACH_IMAGE,
                    mLabel
            );

            mAttachment.setImageBitmap(imageBitmap);
            mAttachmentLayout.setVisibility(View.VISIBLE);
            updateButtonState();
        }
    }
/**
     * Sets the audio of the attachment view.
     *
     * @param audioByte the audio file
     */
    public void setAttachment(byte[] audioByte) {
        if (audioByte == null) clearAttachment();
        else {
            audioAttachment = audioByte;
            mAttachmentLayout.setVisibility(View.GONE);
            updateButtonState();
        }
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    private class ImageLoaderFromCameraTask extends AsyncTask<Void, Void, Bitmap> {

        @TargetApi(16)
        @Override
        protected Bitmap doInBackground(Void... params) {// Get the dimensions of the View
            int targetW = mAttachment.getMaxWidth();
            int targetH = mAttachment.getMaxHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            setAttachment(bitmap);
            mCurrentPhotoPath = null;
        }
    }

    private class ImageLoaderTask extends AsyncTask<Uri, Void, Void> {
        final Context mContext;
        final Uri mUri;
        final Handler mHandler;

        public ImageLoaderTask(final Context context, final Uri uri) {
            mContext = context;
            mUri = uri;
            mHandler = new Handler();
        }

        public void execute() {
            execute(mUri);
        }

        @Override
        protected Void doInBackground(Uri... params) {

            if (params.length < 1) {
                Log.e(TAG, "ImageLoaderTask called with no Uri");
                return null;
            }

            try {
                Uri uri = params[0];

                // Decode the image from the Uri into a bitmap [1], and shrink it
                // according to the user's settings.
                // [1]: http://stackoverflow.com/questions/13930009/how-can-i-get-an-image-from-another-application
                InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                long maxAttachmentSize =
                        SmsHelper.getSendSettings(mContext).getMaxAttachmentSize();
                bitmap = ImageUtils.shrink(bitmap, 90, maxAttachmentSize);

                // Now, rotation the bitmap according to the Exif data.
                final int rotation = ImageUtils.getOrientation(mContext, uri);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                // Can't post UI updates on a background thread.
                final Bitmap imageBitmap = bitmap;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setAttachment(imageBitmap);
                    }
                });

            } catch (FileNotFoundException | NullPointerException e) {
                // Make a toast to the user that the file they've requested to view
                // isn't available.
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                mContext,
                                mRes.getString(R.string.error_file_not_found),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
            return null;
        }
    }

    private class AudioLoaderTask extends AsyncTask<InputStream, Void, Void> {
        final Context mContext;
        final InputStream mFile;
        final Handler mHandler;
        final Boolean mSendDirectly;

        public AudioLoaderTask(final Context context, final InputStream file, Boolean sendDirectly) {
            mContext = context;
            mFile = file;
            mHandler = new Handler();
            mSendDirectly = sendDirectly;
        }

        public void execute() {
            execute(mFile);
        }

        @Override
        protected Void doInBackground(InputStream... params) {

            if (params.length < 1) {
                Log.e(TAG, "AudioLoaderTask called with no File");
                return null;
            }
            InputStream file = params[0];
            if (file == null) {
                Log.e(TAG, "No such audio-file");
                //fixme feedback for user
                return null;
            }

            byte[] byteArray;
            try {byteArray = ByteStreams.toByteArray(file);}
            catch (IOException e) {
                Log.e(TAG, "Could not convert file to ByteArray: " + e);
                //todo Toast for user
                return null;
            }

            long maxAttachmentSize = SmsHelper.getSendSettings(mContext).getMaxAttachmentSize();
            if (byteArray != null && byteArray.length < maxAttachmentSize) {
                // Can't post UI updates on a background thread.
                final byte[] audioByte = byteArray;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSendDirectly) sendSms(audioByte);
                        else setAttachment(audioByte);
                    }
                });
            } else Log.i(TAG, "Audio-file is too big"); //fixme feedback for user

            return null;
        }
    }

    @Override
    public void refresh() {
        mButtonBackground.setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
        mButtonBar1.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.MULTIPLY);
        mButtonBar2.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.MULTIPLY);
        mAttachmentPanel.setBackgroundColor(ThemeManager.getColor());
        mAttach.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.MULTIPLY);
        mCamera.setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.MULTIPLY);
        updateDelayButton();
        mProgress.setUnfinishedStrokeColor(ThemeManager.getTextOnColorSecondary());
        mProgress.setFinishedStrokeColor(ThemeManager.getTextOnColorPrimary());
        if (ThemeManager.getSentBubbleRes() != 0) mReplyText.setBackgroundResource(ThemeManager.getSentBubbleRes());
        mReplyText.getBackground().setColorFilter(ThemeManager.getNeutralBubbleColor(), PorterDuff.Mode.MULTIPLY);
        mReplyText.refresh();
        getBackground().setColorFilter(ThemeManager.getBackgroundColor(), PorterDuff.Mode.MULTIPLY);
    }

    private void updateDelayButton() {
        mDelay.setColorFilter(mDelayedMessagingEnabled ?
                        ThemeManager.getTextOnColorPrimary() : ThemeManager.getTextOnColorSecondary(),
                PorterDuff.Mode.MULTIPLY);
    }
}
