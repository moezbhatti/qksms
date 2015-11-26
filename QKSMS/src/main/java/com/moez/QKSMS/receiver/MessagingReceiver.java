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

public class MessagingReceiver extends BroadcastReceiver {
    private final String TAG = "MessagingReceiver";

    String address;
    String body;
    long date;

    Uri uri;

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
                body = sms.getDisplayMessageBody();
            } else {
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                body = bodyText.toString();
            }

            address = sms.getDisplayOriginatingAddress();
            date = sms.getTimestampMillis();

            uri = SmsHelper.addMessageToInbox(context, address, body, date);

            Message message = new Message(context, uri);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(context, message.getThreadId());

            if (BlockedConversationHelper.isFutureBlocked(prefs, address)) {
                BlockedConversationHelper.unblockFutureConversation(prefs, address);
                BlockedConversationHelper.blockConversation(prefs, message.getThreadId());
                message.markSeen();
                BlockedConversationHelper.FutureBlockedConversationObservable.getInstance().futureBlockedConversationReceived();

            } else if (conversationPrefs.getNotificationsEnabled() && !BlockedConversationHelper.getBlockedConversationIds(
                    PreferenceManager.getDefaultSharedPreferences(context)).contains(message.getThreadId())) {
                Intent messageHandlerIntent = new Intent(context, NotificationService.class);
                messageHandlerIntent.putExtra(NotificationService.EXTRA_POPUP, true);
                messageHandlerIntent.putExtra(NotificationService.EXTRA_URI, uri.toString());
                context.startService(messageHandlerIntent);

                UnreadBadgeService.update(context);
                NotificationManager.create(context);
            } else {
                message.markSeen();
            }

            if (conversationPrefs.getWakePhoneEnabled()) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "MessagingReceiver");
                wakeLock.acquire();
                wakeLock.release();
            }
        }
    }
}
