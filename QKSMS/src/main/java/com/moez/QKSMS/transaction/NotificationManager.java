package com.moez.QKSMS.transaction;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.android.mms.transaction.TransactionService;
import com.android.mms.transaction.TransactionState;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.utils.ImageUtils;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.model.ImageModel;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.receiver.WearableIntentReceiver;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.messagelist.MessageItem;
import com.moez.QKSMS.ui.popup.QKComposeActivity;
import com.moez.QKSMS.ui.popup.QKReplyActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NotificationManager {
    private static final String TAG = "NotificationManager";

    private static final int NOTIFICATION_ID_QUICKCOMPOSE = 4516;
    private static final int NOTIFICATION_ID_FAILED = 4295;

    public static final String ACTION_MARK_READ = "com.moez.QKSMS.MARK_READ";
    public static final String ACTION_MARK_SEEN = "com.moez.QKSMS.MARK_SEEN";

    private static final String DEFAULT_RINGTONE = "content://settings/system/notification_sound";

    private static final String PREV_NOTIFICATIONS = "key_prev_notifications";

    private static final long[] VIBRATION = {0, 200, 200, 200};
    private static final long[] VIBRATION_SILENT = {0, 0};

    private static HandlerThread sThread;
    private static Looper sLooper;
    private static Handler sHandler;

    private static SharedPreferences sPrefs;
    private static Resources sRes;

    static {
        // Start a new thread for showing notifications on with minimum priority
        sThread = new HandlerThread("NotificationManager");
        sThread.start();
        sThread.setPriority(HandlerThread.MIN_PRIORITY);

        sLooper = sThread.getLooper();
        sHandler = new Handler(sLooper);
    }

    public static void init(final Context context) {

        // Initialize the static shared prefs and resources.
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sRes = context.getResources();

        // Listen for MMS events.
        IntentFilter filter = new IntentFilter(TransactionService.TRANSACTION_COMPLETED_ACTION);
        context.registerReceiver(sBroadcastReceiver, filter);
    }

    private static BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO this should happen on a background thread because it requires a DB query
            int result = intent.getIntExtra(TransactionService.STATE, TransactionState.FAILED);
            Uri uri = intent.getParcelableExtra(TransactionService.STATE_URI);

            if (uri != null) {
                // Get the message type
                int msgType = -1;
                Cursor c = null;
                try {
                    c = SqliteWrapper.query(context, context.getContentResolver(), uri,
                            new String[]{Telephony.Mms.MESSAGE_TYPE}, null, null, null
                    );
                    if (c.moveToFirst()) {
                        msgType = c.getInt(c.getColumnIndex(Telephony.Mms.MESSAGE_TYPE));
                    }
                } finally {
                    if (c != null) c.close();
                }

                // For successful retrieve messages, show a notification!
                if (result == TransactionState.SUCCESS && msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) {
                    create(context);
                } else {
                    update(context);
                }
            }
        }
    };

    /**
     * Creates a new notification, called when a new message is received. This notification will have sound and
     * vibration
     */
    public static void create(final Context context) {
        if (sPrefs.getBoolean(SettingsFragment.NOTIFICATIONS, true)) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    HashMap<Long, ArrayList<MessageItem>> conversations = SmsHelper.getUnreadUnseenConversations(context);

                    // Let's find the list of current notifications. If we're showing multiple notifications, now we know
                    // which ones don't need to be touched
                    Set<Long> oldThreads = new HashSet<>();
                    for (String s : sPrefs.getStringSet(PREV_NOTIFICATIONS, new HashSet<String>())) {
                        long l = Long.parseLong(s);
                        if (!oldThreads.contains(l)) {
                            oldThreads.add(l);
                        }
                    }

                    dismissOld(context, conversations);

                    // If there are no messages, don't try to create a notification
                    if (conversations.size() == 0) {
                        return;
                    }

                    ArrayList<MessageItem> lastConversation = conversations.get(conversations.keySet().toArray()[0]);
                    MessageItem lastMessage = lastConversation.get(0);

                    // If this message is in the foreground, mark it as read
                    Message message = new Message(context, lastMessage.mMsgId);
                    if (message.getThreadId() == MainActivity.sThreadShowing) {
                        message.markRead();
                        return;
                    }

                    long threadId = (long) conversations.keySet().toArray()[0];
                    ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(context, threadId);

                    if (!conversationPrefs.getNotificationsEnabled()) {
                        return;
                    }

                    // Otherwise, reset the state and show the notification.
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setPriority(getNotificationPriority(context))
                                    .setSound(conversationPrefs.getNotificationSoundUri())
                                    .setVibrate(VIBRATION_SILENT)
                                    .setAutoCancel(true);

                    if (conversationPrefs.getVibrateEnabled()) {
                        builder.setVibrate(VIBRATION);
                    }

                    if (conversationPrefs.getNotificationLedEnabled()) {
                        builder.setLights(getLedColor(conversationPrefs), 1000, 1000);
                    }

                    Integer privateNotifications = conversationPrefs.getPrivateNotificationsSetting();

                    if (conversationPrefs.getTickerEnabled()) {
                        switch (privateNotifications) {
                            case 0:
                                builder.setTicker(String.format("%s: %s", lastMessage.mContact, lastMessage.mBody));
                                break;
                            case 1:
                                builder.setTicker(String.format("%s: %s", lastMessage.mContact, sRes.getString(R.string.new_message)));
                                break;
                            case 2:
                                builder.setTicker(String.format("%s: %s", "QKSMS", sRes.getString(R.string.new_message)));
                                break;
                        }
                    }

                    if (conversationPrefs.getWakePhoneEnabled()) {
                        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "FlashActivity");
                        wl.acquire();
                        wl.release();
                    }

                    if (conversations.size() == 1 && lastConversation.size() == 1) {
                        singleMessage(context, lastConversation, threadId, builder, conversationPrefs, privateNotifications);
                    } else if (conversations.size() == 1) {
                        singleSender(context, lastConversation, threadId, builder, conversationPrefs, privateNotifications);
                    } else {
                        multipleSenders(context, conversations, oldThreads, builder);
                    }
                }
            });
        }
    }

    /**
     * Updates the notifications silently. This is called when a conversation is marked read or something like that,
     * where we need to update the notifications without alerting the user
     */
    public static void update(final Context context) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                HashMap<Long, ArrayList<MessageItem>> conversations = SmsHelper.getUnreadUnseenConversations(context);

                // Let's find the list of current notifications. If we're showing multiple notifications, now we know
                // which ones don't need to be touched
                Set<Long> oldThreads = new HashSet<>();
                for (String s : sPrefs.getStringSet(PREV_NOTIFICATIONS, new HashSet<String>())) {
                    long l = Long.parseLong(s);
                    if (!oldThreads.contains(l)) {
                        oldThreads.add(l);
                    }
                }

                dismissOld(context, conversations);

                // If there are no messages, don't try to create a notification
                if (conversations.size() == 0) {
                    return;
                }

                ArrayList<MessageItem> lastConversation = conversations.get(conversations.keySet().toArray()[0]);
                MessageItem lastMessage = lastConversation.get(0);

                // If the message is visible (i.e. it is currently showing in the Main Activity),
                // don't show a notification; just mark it as read and return.
                Message message = new Message(context, lastMessage.mMsgId);
                if (message.getThreadId() == MainActivity.sThreadShowing) {
                    message.markRead();
                    return;
                }

                long threadId = (long) conversations.keySet().toArray()[0];
                ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(context, threadId);

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notification)
                                        // SMS messages are high priority
                                .setPriority(getNotificationPriority(context))
                                        // Silent here because this is just an update, not a new
                                        // notification
                                .setSound(null)
                                .setVibrate(VIBRATION_SILENT)
                                .setAutoCancel(true);

                if (conversationPrefs.getNotificationLedEnabled()) {
                    builder.setLights(getLedColor(conversationPrefs), 1000, 1000);
                }

                Integer privateNotifications = conversationPrefs.getPrivateNotificationsSetting();

                if (conversations.size() == 1 && lastConversation.size() == 1) {
                    singleMessage(context, lastConversation, threadId, builder, conversationPrefs, privateNotifications);
                } else if (conversations.size() == 1) {
                    singleSender(context, lastConversation, threadId, builder, conversationPrefs, privateNotifications);
                } else {
                    multipleSenders(context, conversations, oldThreads, builder);
                }
            }
        });
    }

    /**
     * Creates a notification to tell the user about failed messages. This is currently pretty shitty and needs to be
     * improved, by adding functionality such as the ability to delete all of the failed messages
     */
    public static void notifyFailed(final Context context) {
        sHandler.post(new Runnable() {
            public void run() {
                Cursor failedCursor = context.getContentResolver().query(
                        SmsHelper.SMS_CONTENT_PROVIDER,
                        new String[]{SmsHelper.COLUMN_THREAD_ID},
                        SmsHelper.FAILED_SELECTION,
                        null, null
                );

                // Dismiss the notification if the failed cursor doesn't have any items in it.
                if (failedCursor == null || !failedCursor.moveToFirst() || failedCursor.getCount() <= 0) {
                    dismiss(context, NOTIFICATION_ID_FAILED);
                    return;
                }

                String title;
                PendingIntent PI;
                if (failedCursor.getCount() == 1) {
                    title = sRes.getString(R.string.failed_message);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_THREAD_ID, failedCursor.getLong(0));
                    PI = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    title = failedCursor.getCount() + " " + sRes.getString(R.string.failed_messages);
                    Intent intent = new Intent(context, MainActivity.class);
                    PI = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                }


                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                for (Message message : SmsHelper.getFailedMessages(context)) {
                    switch (Integer.parseInt(sPrefs.getString(SettingsFragment.PRIVATE_NOTIFICATION, "0"))) {
                        case 0:
                            inboxStyle.addLine(Html.fromHtml("<strong>" + message.getName() + "</strong> " + message.getBody()));
                            break;
                        case 1:
                            inboxStyle.addLine(Html.fromHtml("<strong>" + message.getName() + "</strong> " + sRes.getString(R.string.new_message)));
                            break;
                        case 2:
                            inboxStyle.addLine(Html.fromHtml("<strong>" + "QKSMS" + "</strong> " + sRes.getString(R.string.new_message)));
                            break;
                    }
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_failed)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(Uri.parse(sPrefs.getString(SettingsFragment.NOTIFICATION_TONE, DEFAULT_RINGTONE)))
                        .setVibrate(VIBRATION_SILENT)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setStyle(inboxStyle)
                        .setContentText(sRes.getString(R.string.failed_messages_summary))
                        .setLargeIcon(BitmapFactory.decodeResource(sRes, R.drawable.ic_notification_failed))
                        .setContentIntent(PI)
                        .setNumber(failedCursor.getCount());

                if (sPrefs.getBoolean(SettingsFragment.NOTIFICATION_VIBRATE, false)) {
                    builder.setVibrate(VIBRATION);
                }

                if (sPrefs.getBoolean(SettingsFragment.NOTIFICATION_LED, true)) {
                    builder.setLights(getLedColor(new ConversationPrefsHelper(context, 0)), 1000, 1000);
                }

                if (sPrefs.getBoolean(SettingsFragment.NOTIFICATION_TICKER, false)) {
                    builder.setTicker(title);
                }

                NotificationManager.notify(context, NOTIFICATION_ID_FAILED, builder.build());
            }
        });
    }


    /**
     * Notifies the user of the given notification.
     */
    public static void notify(Context context, int id, android.app.Notification notification) {
        ((android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(id, notification);
    }

    /**
     * Cancels the notification for the given ID.
     */
    public static void dismiss(Context context, int id) {
        // Cancel the notification for this ID.
        ((android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(id);
    }

    /**
     * Dismisses all old notifications. The purpose of this is to clear notifications that don't need to show up,
     * without making the remaining ones dissapear and pop up again like how NotificationMangager.cancelAll and then
     * rebuilding them would do
     * <p>
     * This should stay private, because it assumes that the preferences have already been initialized
     */
    private static void dismissOld(Context context, HashMap<Long, ArrayList<MessageItem>> newMessages) {
        // Let's find the list of current notifications
        Set<Long> oldThreads = new HashSet<>();
        for (String s : sPrefs.getStringSet(PREV_NOTIFICATIONS, new HashSet<String>())) {
            long l = Long.parseLong(s);
            if (!oldThreads.contains(l)) {
                oldThreads.add(l);
            }
        }

        // Now we need a comparable list of thread ids for the new messages
        Set<Long> newThreads = newMessages.keySet();

        Log.d(TAG, "Old threads: " + Arrays.toString(oldThreads.toArray()));
        Log.d(TAG, "New threads: " + Arrays.toString(newThreads.toArray()));

        // For all of the notifications that exist and are not to be still shown, let's dismiss them
        for (long threadId : oldThreads) {
            if (!newThreads.contains(threadId)) {
                dismiss(context, (int) threadId);
            }
        }

        // Now let's convert the new list into a set of strings so we can save them to prefs
        Set<String> newThreadStrings = new HashSet<>();
        for (long threadId : newThreads) {
            newThreadStrings.add(Long.toString(threadId));
        }

        sPrefs.edit().putStringSet(PREV_NOTIFICATIONS, newThreadStrings).apply();
    }

    /**
     * Displays a notification for a single message
     */
    private static void singleMessage(final Context context, final ArrayList<MessageItem> messages, final long threadId,
                                      final NotificationCompat.Builder builder, final ConversationPrefsHelper conversationPrefs,
                                      final Integer privateNotifications) {

        MessageItem message = messages.get(0);

        if (message.isMms()) {
            Log.d(TAG, "Message is MMS");
            if (message.mSlideshow != null) {
                Log.d(TAG, "Woah! Slideshow not null");
                buildSingleMessageNotification(context, messages, threadId, builder, conversationPrefs, privateNotifications);
            } else {
                Log.d(TAG, "Listening for PDU");
                message.setOnPduLoaded(new MessageItem.PduLoadedCallback() {
                    @Override
                    public void onPduLoaded(MessageItem messageItem) {
                        Log.d(TAG, "PDU Loaded");
                        buildSingleMessageNotification(context, messages, threadId, builder, conversationPrefs, privateNotifications);
                    }
                });
            }
        } else {
            buildSingleMessageNotification(context, messages, threadId, builder, conversationPrefs, privateNotifications);
        }
    }


    /**
     * Builds the actual notification for the single message. This code can be called at different points in execution
     * depending on whether or not the MMS data has been downloaded
     */
    private static void buildSingleMessageNotification(final Context context, ArrayList<MessageItem> messages, long threadId,
                                                       final NotificationCompat.Builder builder, ConversationPrefsHelper conversationPrefs,
                                                       final Integer privateNotifications) {

        MessageItem message = messages.get(0);

        Intent replyIntent = new Intent(context, QKReplyActivity.class);
        replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        replyIntent.putExtra(QKReplyActivity.EXTRA_THREAD_ID, threadId);
        replyIntent.putExtra(QKReplyActivity.EXTRA_SHOW_KEYBOARD, true);
        final PendingIntent replyPI = PendingIntent.getActivity(context, buildRequestCode(threadId, 0), replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent threadIntent = new Intent(context, MainActivity.class);
        threadIntent.putExtra(MainActivity.EXTRA_THREAD_ID, threadId);
        final PendingIntent threadPI = PendingIntent.getActivity(context, buildRequestCode(threadId, 1), threadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent readIntent = new Intent(ACTION_MARK_READ);
        readIntent.putExtra("thread_id", threadId);
        final PendingIntent readPI = PendingIntent.getBroadcast(context, buildRequestCode(threadId, 2), readIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent seenIntent = new Intent(ACTION_MARK_SEEN);
        final PendingIntent seenPI = PendingIntent.getBroadcast(context, buildRequestCode(threadId, 4), seenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int unreadMessageCount = SmsHelper.getUnreadMessageCount(context);
        String body;
        String title;
        NotificationCompat.Style nstyle = null;
        switch (privateNotifications) {
            case 0: //Hide nothing
                body = message.mBody;
                title = message.mContact;
                nstyle = new NotificationCompat.BigTextStyle().bigText(message.mBody);
                break;
            case 1: //Hide message
                body = sRes.getString(R.string.new_message);
                title = message.mContact;
                break;
            case 2: //Hide sender & message
                body = sRes.getString(R.string.new_message);
                title = "QKSMS";
                break;
            default:
                body = message.mBody;
                title = message.mContact;
                nstyle = null;
        }

        builder.setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(getLargeIcon(context, Contact.get(message.mAddress, false), privateNotifications))
                .setContentIntent(threadPI)
                .setNumber(unreadMessageCount)
                .setStyle(nstyle)
                .addAction(R.drawable.ic_reply, sRes.getString(R.string.reply), replyPI)
                .addAction(R.drawable.ic_accept, sRes.getString(R.string.read), readPI)
                .extend(WearableIntentReceiver.getSingleConversationExtender(context, message.mContact, message.mAddress, threadId))
                .setDeleteIntent(seenPI);
        if (conversationPrefs.getDimissedReadEnabled()) {
            builder.setDeleteIntent(readPI);
        }

        if (conversationPrefs.getCallButtonEnabled()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + message.mAddress));
            PendingIntent callPI = PendingIntent.getActivity(context, buildRequestCode(threadId, 3), callIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_call, sRes.getString(R.string.call), callPI);
        }

        if (message.isMms()) {
            Log.d(TAG, "Message is MMS");

            SlideshowModel model = message.mSlideshow;

            if (model != null && model.isSimple() && model.get(0).getImage() != null) {
                Log.d(TAG, "MMS type: image");
                ImageModel imageModel = model.get(0).getImage();
                Bitmap image = imageModel.getBitmap(imageModel.getWidth(), imageModel.getHeight());
                NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(message.mContact)
                        .setSummaryText(message.mBody)
                        .bigLargeIcon(getLargeIcon(context, Contact.get(message.mAddress, false), privateNotifications))
                        .bigPicture(image);
                if (privateNotifications == 0) builder.setStyle(style);
                else builder.setStyle(null);

            } else {
                Log.d(TAG, "MMS Type: not an image lol");
                if (privateNotifications == 0)
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.mBody));
                else builder.setStyle(null);
            }

        }

        NotificationManager.notify(context, (int) threadId, builder.build());
    }

    /**
     * Creates a notification that contains several messages that are all part of the same conversation
     */
    private static void singleSender(final Context context, ArrayList<MessageItem> messages, long threadId,
                                     final NotificationCompat.Builder builder, ConversationPrefsHelper conversationPrefs,
                                     final Integer privateNotifications) {

        MessageItem message = messages.get(0);

        Intent replyIntent = new Intent(context, QKReplyActivity.class);
        replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        replyIntent.putExtra(QKReplyActivity.EXTRA_THREAD_ID, threadId);
        replyIntent.putExtra(QKReplyActivity.EXTRA_SHOW_KEYBOARD, true);
        PendingIntent replyPI = PendingIntent.getActivity(context, buildRequestCode(threadId, 0), replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent threadIntent = new Intent(context, MainActivity.class);
        threadIntent.putExtra(MainActivity.EXTRA_THREAD_ID, threadId);
        PendingIntent threadPI = PendingIntent.getActivity(context, buildRequestCode(threadId, 1), threadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent readIntent = new Intent(ACTION_MARK_READ);
        readIntent.putExtra("thread_id", threadId);
        PendingIntent readPI = PendingIntent.getBroadcast(context, buildRequestCode(threadId, 2), readIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent seenIntent = new Intent(ACTION_MARK_SEEN);
        PendingIntent seenPI = PendingIntent.getBroadcast(context, buildRequestCode(threadId, 4), seenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (MessageItem message1 : messages) {
            inboxStyle.addLine(message1.mBody);
        }

        String notificationTitle = message.mContact;

        if (!(privateNotifications == 0)) inboxStyle = null;
        if (privateNotifications == 2) notificationTitle = "QKSMS";

        int unreadMessageCount = SmsHelper.getUnreadMessageCount(context);
        builder.setContentTitle(notificationTitle)
                .setContentText(SmsHelper.getUnseenSMSCount(context, threadId) + " " + sRes.getString(R.string.new_messages))
                .setLargeIcon(getLargeIcon(context, Contact.get(message.mAddress, false), privateNotifications))
                .setContentIntent(threadPI)
                .setNumber(unreadMessageCount)
                .setStyle(inboxStyle)
                .addAction(R.drawable.ic_reply, sRes.getString(R.string.reply), replyPI)
                .addAction(R.drawable.ic_accept, sRes.getString(R.string.read), readPI)
                .extend(WearableIntentReceiver.getSingleConversationExtender(context, message.mContact, message.mAddress, threadId))
                .setDeleteIntent(seenPI);

        if (conversationPrefs.getCallButtonEnabled()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + message.mAddress));
            PendingIntent callPI = PendingIntent.getActivity(context, buildRequestCode(threadId, 3), callIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_call, sRes.getString(R.string.call), callPI);
        }

        notify(context, (int) threadId, builder.build());
    }

    /**
     * Creates a unique action ID for notification actions (Open, Mark read, Call, etc)
     */
    private static int buildRequestCode(long threadId, int action) {
        return (int) (action * 100000 + threadId);
    }

    /**
     * Create notifications for multiple conversations
     */
    private static void multipleSenders(Context context, HashMap<Long, ArrayList<MessageItem>> conversations, Set<Long> oldThreads, NotificationCompat.Builder builder) {
        Set<Long> threadIds = conversations.keySet();
        for (long threadId : threadIds) {
            if (!oldThreads.contains(threadId)) {
                ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(context, threadId);
                Integer privateNotification = conversationPrefs.getPrivateNotificationsSetting();
                if (conversations.get(threadId).size() == 1) {
                    singleMessage(context, conversations.get(threadId), threadId, copyBuilder(builder), conversationPrefs, privateNotification);
                } else {
                    singleSender(context, conversations.get(threadId), threadId, copyBuilder(builder), conversationPrefs, privateNotification);
                }
            }
        }
    }

    /**
     * Creates a clone of the NotificationCompat.Builder to be used when we're displaying multiple notifications,
     * and need multiple instances of the builder
     */
    private static NotificationCompat.Builder copyBuilder(NotificationCompat.Builder builder) {
        return new NotificationCompat.Builder(builder.mContext)
                .setSmallIcon(builder.mNotification.icon)
                .setPriority(builder.mNotification.priority)
                .setSound(builder.mNotification.sound)
                .setVibrate(builder.mNotification.vibrate)
                .setLights(builder.mNotification.ledARGB, builder.mNotification.ledOnMS, builder.mNotification.ledOffMS)
                .setTicker(builder.mNotification.tickerText)
                .setAutoCancel(true);
    }

    /**
     * Set up the QK Compose notification
     *
     * @param override       If true, then show the QK Compose notification regardless of the user's preference
     * @param overrideCancel If true, dismiss the notification no matter what
     */
    public static void initQuickCompose(Context context, boolean override, boolean overrideCancel) {

        if (sPrefs == null) {
            init(context);
        }

        if (sPrefs.getBoolean(SettingsFragment.QUICKCOMPOSE, false) || override) {
            Intent composeIntent = new Intent(context, QKComposeActivity.class);
            PendingIntent composePI = PendingIntent.getActivity(context, 9, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle(sRes.getString(R.string.quickcompose))
                    .setContentText(sRes.getString(R.string.quickcompose_detail))
                    .setOngoing(true)
                    .setContentIntent(composePI)
                    .setSmallIcon(R.drawable.ic_compose)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setColor(ThemeManager.getColor());

            NotificationManager.notify(context, NOTIFICATION_ID_QUICKCOMPOSE, builder.build());
        } else {
            dismiss(context, NOTIFICATION_ID_QUICKCOMPOSE);
        }

        if (overrideCancel) {
            dismiss(context, NOTIFICATION_ID_QUICKCOMPOSE);
        }
    }

    private static int getLedColor(ConversationPrefsHelper conversationPrefs) {
        int color = Integer.parseInt(conversationPrefs.getNotificationLedColor());

        if (color == sRes.getColor(R.color.blue_light) || color == sRes.getColor(R.color.blue_dark))
            return sRes.getColor(R.color.blue_dark);
        if (color == sRes.getColor(R.color.purple_light) || color == sRes.getColor(R.color.purple_dark))
            return sRes.getColor(R.color.purple_dark);
        if (color == sRes.getColor(R.color.green_light) || color == sRes.getColor(R.color.green_dark))
            return sRes.getColor(R.color.green_dark);
        if (color == sRes.getColor(R.color.yellow_light) || color == sRes.getColor(R.color.yellow_dark))
            return sRes.getColor(R.color.yellow_dark);
        if (color == sRes.getColor(R.color.red_light) || color == sRes.getColor(R.color.red_dark))
            return sRes.getColor(R.color.red_dark);

        return sRes.getColor(R.color.white_pure);
    }

    /**
     * Retreives the avatar to be used for the notification's large icon. If the user is running Lollipop, then let's
     * crop their avatar to a circle
     */
    private static Bitmap getLargeIcon(Context context, Contact contact, Integer privateNotification) {
        Drawable avatarDrawable = contact.getAvatar(context, new BitmapDrawable(sRes, ContactHelper.blankContact(context, contact.getName())));
        int idealIconWidth = sRes.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int idealIconHeight = sRes.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) avatarDrawable).getBitmap(), idealIconWidth, idealIconHeight, true);

        if (privateNotification == 2) {
            return null;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return ImageUtils.getCircleBitmap(bitmap, idealIconWidth);
            } else {
                return bitmap;
            }
        }
    }

    /**
     * Returns the notification priority we should be using based on whether or not the Heads-up notification should
     * show
     */
    private static int getNotificationPriority(Context context) {
        boolean qkreplyEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SettingsFragment.QUICKREPLY, true);
        if (qkreplyEnabled) {
            return NotificationCompat.PRIORITY_DEFAULT;
        } else {
            return NotificationCompat.PRIORITY_HIGH;
        }
    }
}
