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
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.service.NotificationService;
import com.moez.QKSMS.transaction.NotificationManager;
import com.moez.QKSMS.transaction.SmsHelper;
import org.mistergroup.muzutozvednout.ShouldIAnswerBinder;

public class MessagingReceiver extends BroadcastReceiver {
    private final String TAG = "MessagingReceiver";

    private String mAddress;
    private String mBody;
    private long mDate;

    private Uri mUri;

    @Override
    public void onReceive(Context context, Intent intent) {
        abortBroadcast();

        Log.i(TAG, "Received text message");

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

            mUri = SmsHelper.addMessageToInbox(context, mAddress, mBody, mDate);

            Message message = new Message(context, mUri);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(context, message.getThreadId());

            // The user has set messages from this address to be blocked, but we at the time there weren't any
            // messages from them already in the database, so we couldn't block any thread URI. Now that we have one,
            // we can block it, so that the conversation list adapter knows to ignore this thread in the main list
            if (BlockedConversationHelper.isFutureBlocked(prefs, mAddress)) {
                BlockedConversationHelper.unblockFutureConversation(prefs, mAddress);
                BlockedConversationHelper.blockConversation(prefs, message.getThreadId());
                message.markSeen();
                BlockedConversationHelper.FutureBlockedConversationObservable.getInstance().futureBlockedConversationReceived();

                // If we have notifications enabled and this conversation isn't blocked
            } else if (conversationPrefs.getNotificationsEnabled() && !BlockedConversationHelper.getBlockedConversationIds(
                    PreferenceManager.getDefaultSharedPreferences(context)).contains(message.getThreadId())) {
                Intent messageHandlerIntent = new Intent(context, NotificationService.class);
                messageHandlerIntent.putExtra(NotificationService.EXTRA_POPUP, true);
                messageHandlerIntent.putExtra(NotificationService.EXTRA_URI, mUri.toString());
                context.startService(messageHandlerIntent);

                UnreadBadgeService.update(context);
                NotificationManager.create(context);

            } else { // We shouldn't show a notification for this message
                message.markSeen();
            }

            if (conversationPrefs.getWakePhoneEnabled()) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "MessagingReceiver");
                wakeLock.acquire();
                wakeLock.release();
            }
        }

        ShouldIAnswerBinder shouldIAnswerBinder = new ShouldIAnswerBinder();
        shouldIAnswerBinder.setCallback(new ShouldIAnswerBinder.Callback() {
            @Override
            public void onNumberRating(String number, int rating) {
                Log.d(TAG, "onNumberRating " + number + ": " + String.valueOf(rating));
                shouldIAnswerBinder.unbind(context.getApplicationContext());
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
    }
}
