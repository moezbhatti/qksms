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


class NotificationHelper(val context: Context) {

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

    fun update() {
        val realm = Realm.getDefaultInstance()

        val conversation = realm.where(Conversation::class.java).findFirst()
        if (conversation != null) {
            val messages = realm.where(Message::class.java).equalTo("threadId", conversation.id).findAllSorted("date")

            // A conversation title can also be set here
            // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
            val style = NotificationCompat.MessagingStyle("Me")
            messages.take(NotificationCompat.MessagingStyle.MAXIMUM_RETAINED_MESSAGES).forEach { message ->
                style.addMessage(message.body, message.date, if (message.isMe()) null else "Person")
            }

            val notification = NotificationCompat.Builder(context, "channel_1")
                    .setColor(context.resources.getColor(R.color.colorPrimary))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(style)

            notificationManager.notify(0, notification.build())
        }

        realm.close()
    }

}