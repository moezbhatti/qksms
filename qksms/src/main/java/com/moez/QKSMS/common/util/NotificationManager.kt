/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.common.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import android.support.v4.app.TaskStackBuilder
import com.moez.QKSMS.R
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.domain.interactor.MarkUnarchived
import com.moez.QKSMS.presentation.compose.ComposeActivity
import com.moez.QKSMS.receiver.MarkReadReceiver
import com.moez.QKSMS.receiver.MarkSeenReceiver
import com.moez.QKSMS.receiver.RemoteMessagingReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
        private val context: Context,
        private val prefs: Preferences,
        private val colors: Colors,
        private val messageRepo: MessageRepository,
        private val markUnarchived: MarkUnarchived
) {

    companion object {
        val DEFAULT_CHANNEL_ID = "channel_1"
        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        @SuppressLint("NewApi")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val name = "Message notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(DEFAULT_CHANNEL_ID, name, importance).apply {
                description = "Message notifications description"
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = VIBRATE_PATTERN
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Updates the notification for a particular conversation
     */
    fun update(threadId: Long) {
        // If notifications are disabled, don't do anything
        if (!prefs.notifications.get()) {
            return
        }

        val messages = messageRepo.getUnreadUnseenMessages(threadId)

        // If there are no messages to be displayed, make sure that the notification is dismissed
        if (messages.isEmpty()) {
            notificationManager.cancel(threadId.toInt())
            return
        }

        val conversation = messageRepo.getConversation(threadId) ?: return

        val style = NotificationCompat.MessagingStyle("Me")
        messages.forEach { message ->
            val name = if (message.isMe()) null else conversation.getTitle()
            style.addMessage(message.body, message.date, name)
        }

        val contentIntent = Intent(context, ComposeActivity::class.java).putExtra("threadId", threadId)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addParentStack(ComposeActivity::class.java)
        taskStackBuilder.addNextIntent(contentIntent)
        val contentPI = taskStackBuilder.getPendingIntent(threadId.toInt() + 10000, PendingIntent.FLAG_UPDATE_CURRENT)

        val seenIntent = Intent(context, MarkSeenReceiver::class.java).putExtra("threadId", threadId)
        val seenPI = PendingIntent.getBroadcast(context, threadId.toInt() + 20000, seenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val readIntent = Intent(context, MarkReadReceiver::class.java).putExtra("threadId", threadId)
        val readPI = PendingIntent.getBroadcast(context, threadId.toInt() + 30000, readIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val readAction = NotificationCompat.Action(R.drawable.ic_done_black_24dp, context.getString(R.string.notification_read), readPI)

        val notification = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setColor(colors.theme.blockingFirst())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setNumber(messages.size)
                .setAutoCancel(true)
                .setContentIntent(contentPI)
                .setDeleteIntent(seenPI)
                .addAction(readAction)
                .setStyle(style)
                .setVibrate(if (prefs.vibration.get()) VIBRATE_PATTERN else longArrayOf(0))

        if (Build.VERSION.SDK_INT >= 24) {
            notification.addAction(getReplyAction(conversation.recipients[0]?.address.orEmpty(), threadId))
        }

        notificationManager.notify(threadId.toInt(), notification.build())
    }

    private fun getReplyAction(address: String, threadId: Long): NotificationCompat.Action {
        val replyIntent = Intent(context, RemoteMessagingReceiver::class.java)
                .putExtra("address", address)
                .putExtra("threadId", threadId)
        val replyPI = PendingIntent.getBroadcast(context, threadId.toInt() + 40000, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val responseSet = arrayListOf("Hello", "Hey").toTypedArray().sortedArray()
        val remoteInput = RemoteInput.Builder("body")
                .setLabel(context.getString(R.string.notification_reply))
                .setChoices(responseSet)
                .build()

        return NotificationCompat.Action.Builder(
                R.drawable.ic_reply_black_24dp,
                context.getString(R.string.notification_reply), replyPI)
                .addRemoteInput(remoteInput)
                .build()
    }

}