package com.moez.QKSMS.common.util

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
import com.moez.QKSMS.R
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.receiver.MarkReadReceiver
import com.moez.QKSMS.receiver.MarkSeenReceiver
import com.moez.QKSMS.receiver.RemoteMessagingReceiver
import com.moez.QKSMS.presentation.main.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
        private val context: Context,
        private val themeManager: ThemeManager) {

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

                    val contentIntent = Intent(context, MainActivity::class.java).putExtra("threadId", threadId)
                    val contentPI = PendingIntent.getActivity(context, threadId.toInt() + 10000, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val seenIntent = Intent(context, MarkSeenReceiver::class.java).putExtra("threadId", threadId)
                    val seenPI = PendingIntent.getBroadcast(context, threadId.toInt() + 20000, seenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val readIntent = Intent(context, MarkReadReceiver::class.java).putExtra("threadId", threadId)
                    val readPI = PendingIntent.getBroadcast(context, threadId.toInt() + 30000, readIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    val readAction = NotificationCompat.Action(R.drawable.ic_done_black_24dp, context.getString(R.string.notification_read), readPI)

                    val notification = NotificationCompat.Builder(context, "channel_1")
                            .setColor(themeManager.color.blockingFirst())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setNumber(messages.size)
                            .setAutoCancel(true)
                            .setContentIntent(contentPI)
                            .setDeleteIntent(seenPI)
                            .addAction(readAction)
                            .setStyle(style)

                    if (Build.VERSION.SDK_INT >= 24 && conversation != null) {
                        notification.addAction(getReplyAction(conversation.contacts[0]?.address.orEmpty(), conversation.id))
                    }

                    notificationManager.notify(threadId.toInt(), notification.build())
                }
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