package com.moez.QKSMS.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.moez.QKSMS.common.BlockedConversationHelper;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.LifecycleHandler;
import com.moez.QKSMS.common.MessagingHelper;
import com.moez.QKSMS.common.NotificationManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.SmsHelper;
import com.moez.QKSMS.common.utils.PackageUtils;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.service.UnreadBadgeService;
import com.moez.QKSMS.ui.popup.QKReplyActivity;
import org.mistergroup.muzutozvednout.ShouldIAnswerBinder;

public class MessagingReceiver extends BroadcastReceiver {
    private final String TAG = "MessagingReceiver";

    private Context mContext;
    private SharedPreferences mPrefs;

    private String mAddress;
    private String mBody;
    private long mDate;

    private Uri mUri;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        abortBroadcast();

        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (intent.getExtras() != null) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            SmsMessage sms = messages[0];
            if (messages.length == 1 || sms.isReplace()) {
                mBody = sms.getDisplayMessageBody();
            } else {
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                mBody = bodyText.toString();
            }

            mAddress = sms.getDisplayOriginatingAddress();
            mDate = sms.getTimestampMillis();

            if (QKPreferences.getBoolean(QKPreference.SHOULD_I_ANSWER) &&
                    PackageUtils.isAppInstalled(mContext, "org.mistergroup.muzutozvednout")) {

                ShouldIAnswerBinder shouldIAnswerBinder = new ShouldIAnswerBinder();
                shouldIAnswerBinder.setCallback(new ShouldIAnswerBinder.Callback() {
                    @Override
                    public void onNumberRating(String number, int rating) {
                        Log.i(TAG, "onNumberRating " + number + ": " + String.valueOf(rating));
                        shouldIAnswerBinder.unbind(context.getApplicationContext());
                        if (rating != ShouldIAnswerBinder.RATING_NEGATIVE) {
                            insertMessageAndNotify();
                        }
                    }

                    @Override
                    public void onServiceConnected() {
                        try {
                            shouldIAnswerBinder.getNumberRating(mAddress);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onServiceDisconnected() {
                    }
                });

                shouldIAnswerBinder.bind(context.getApplicationContext());
            } else {
                insertMessageAndNotify();
            }
        }
    }

    private void insertMessageAndNotify() {
        mUri = SmsHelper.addMessageToInbox(mContext, mAddress, mBody, mDate);

        Message message = new Message(mContext, mUri);
        ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(mContext, message.getThreadId());

        // The user has set messages from this address to be blocked, but we at the time there weren't any
        // messages from them already in the database, so we couldn't block any thread URI. Now that we have one,
        // we can block it, so that the conversation list adapter knows to ignore this thread in the main list
        if (BlockedConversationHelper.isFutureBlocked(mAddress)) {
            BlockedConversationHelper.unblockFutureConversation(mAddress);
            BlockedConversationHelper.blockConversation(message.getThreadId());
            MessagingHelper.markMessageSeen(mContext, message.getId());
            BlockedConversationHelper.FutureBlockedConversationObservable.getInstance().futureBlockedConversationReceived();

            // If we have notifications enabled and this conversation isn't blocked
        } else if (conversationPrefs.getNotificationsEnabled() && !BlockedConversationHelper.getBlockedConversationIds(
        ).contains(message.getThreadId())) {
            showPopup(message);
            UnreadBadgeService.update(mContext);
            NotificationManager.create(mContext);
        } else { // We shouldn't show a notification for this message
            MessagingHelper.markMessageSeen(mContext, message.getId());
        }

        if (conversationPrefs.getWakePhoneEnabled()) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "MessagingReceiver");
            wakeLock.acquire();
            wakeLock.release();
        }
    }

    private void showPopup(Message message) {
        ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(mContext, message.getThreadId());

        if (conversationPrefs.getNotificationsEnabled()) {
            // Only show QuickReply if we're outside of the app, and they have QuickReply enabled
            if (!LifecycleHandler.isApplicationVisible() && QKPreferences.getBoolean(QKPreference.QK_REPLY)) {
                Intent popupIntent = new Intent(mContext, QKReplyActivity.class);
                popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                popupIntent.putExtra(QKReplyActivity.EXTRA_THREAD_ID, message.getThreadId());
                mContext.startActivity(popupIntent);
            }
        } else {
            // If the conversation is muted, mark this message as "seen". Note that this is different from marking it as "read".
            MessagingHelper.markMessageSeen(mContext, message.getId());
        }
    }
}
