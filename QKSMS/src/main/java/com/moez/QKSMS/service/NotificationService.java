package com.moez.QKSMS.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;

import android.preference.PreferenceManager;
import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.popup.QKReplyActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class NotificationService extends Service {
    private final String TAG = "NotificationService";

    public static final String EXTRA_POPUP = "popup";
    public static final String EXTRA_URI = "uri";
    private Context context = this;
    private Intent popupIntent;
    private SharedPreferences prefs;
    private ConversationPrefsHelper conversationPrefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Uri uri = Uri.parse(intent.getStringExtra(EXTRA_URI));

        // Try to getConversation the message's ID, in case the given Uri is bad.
        long messageId = -1;
        Cursor cursor = context.getContentResolver().query(uri, new String[]{SmsHelper.COLUMN_ID},
                null, null, null);
        if (cursor.moveToFirst()) {
            messageId = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ID));
        }
        cursor.close();

        // Make sure we found a message before showing QuickReply and using PushBullet.
        if (messageId != -1) {

            Message message = new Message(context, messageId);

            conversationPrefs = new ConversationPrefsHelper(context, message.getThreadId());

            if (conversationPrefs.getNotificationsEnabled()) {
                // Only show QuickReply if we're outside of the app, and they have popups and QuickReply enabled.
                if (!QKReplyActivity.sIsShowing && message.getThreadId() != MainActivity.sThreadShowing &&
                        intent.getBooleanExtra(EXTRA_POPUP, false) && prefs.getBoolean(SettingsFragment.QUICKREPLY, true)) {

                    popupIntent = new Intent(context, QKReplyActivity.class);
                    popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    popupIntent.putExtra(QKReplyActivity.EXTRA_THREAD_ID, message.getThreadId());
                    startActivity(popupIntent);
                }

                // Get the photo for the PushBullet notification.
                Bitmap photoBitmap = message.getPhotoBitmap();
                if (photoBitmap == null) {
                    photoBitmap = ContactHelper.blankContact(context, message.getName());
                }

                PushbulletService.mirrorMessage(context, "" + message.getThreadId(),
                        message.getName(), message.getBody(), photoBitmap, null, 6639);
            } else {
                // If the conversation is muted, mark this message as "seen". Note that this is
                // different from marking it as "read".
                message.markSeen();
            }
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
