package com.moez.QKSMS.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.moez.QKSMS.R
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import io.realm.Realm


class NotificationManager(val context: Context) {

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
    fun update() {
        val realm = Realm.getDefaultInstance()

        realm.where(Conversation::class.java).equalTo("read", false).findAllSorted("date").forEach { conversation ->
            val style = NotificationCompat.MessagingStyle("Me")
            realm.where(Message::class.java)
                    .equalTo("threadId", conversation.id)
                    .equalTo("seen", false)
                    .equalTo("read", false)
                    .findAllSorted("date")
                    .forEach { message ->
                        val name = if (message.isMe()) null else conversation.getTitle()
                        style.addMessage(message.body, message.date, name)
                    }

            val notification = NotificationCompat.Builder(context, "channel_1")
                    .setColor(context.resources.getColor(R.color.colorPrimary))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(style)

            notificationManager.notify(conversation.id.toInt(), notification.build())
        }

        realm.close()
    }

}