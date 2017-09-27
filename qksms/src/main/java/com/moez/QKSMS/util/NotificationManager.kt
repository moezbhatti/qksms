package com.moez.QKSMS.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.moez.QKSMS.R
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.receiver.MarkSeenReceiver
import com.moez.QKSMS.ui.conversations.ConversationListActivity


class NotificationManager(private val context: Context, private val themeManager: ThemeManager) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val id = "channel_1"
            val name = "Message notifications"
            val description = "Message notifications description"
            val importance = NotificationManager.IMPORTANCE_MAX
            val channel = NotificationChannel(id, name, importance)

            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.WHITE
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            notificationManager.createNotificationChannel(channel)
        }
    }

    // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
    fun update(messageRepo: MessageRepository) {
        messageRepo.getUnreadUnseenMessages()
                .groupBy { message -> message.threadId }
                .forEach { group ->
                    val threadId = group.key
                    val messages = group.value
                    val conversation = messageRepo.getConversation(threadId)

                    val style = NotificationCompat.MessagingStyle("Me")
                    messages.forEach { message ->
                        val name = if (message.isMe()) null else conversation?.getTitle() ?: ""
                        style.addMessage(message.body, message.date, name)
                    }

                    val contentIntent = Intent(context, ConversationListActivity::class.java).putExtra("threadId", threadId)
                    val contentPI = PendingIntent.getActivity(context, threadId.toInt() + 10000, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val seenIntent = Intent(context, MarkSeenReceiver::class.java).putExtra("threadId", threadId)
                    val seenPI = PendingIntent.getBroadcast(context, threadId.toInt() + 20000, seenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val notification = NotificationCompat.Builder(context, "channel_1")
                            .setColor(themeManager.color)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setNumber(messages.size)
                            .setAutoCancel(true)
                            .setContentIntent(contentPI)
                            .setDeleteIntent(seenPI)
                            .setStyle(style)

                    notificationManager.notify(threadId.toInt(), notification.build())
                }
    }

}