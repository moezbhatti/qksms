package com.moez.QKSMS.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.klinker.android.send_message.BroadcastUtils
import com.moez.QKSMS.model.ScheduledMessage
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import javax.inject.Inject

class ScheduledMessageRepositoryImpl @Inject constructor(
        private val context: Context
) : ScheduledMessageRepository {

    override fun saveScheduledMessage(date: Long, subId: Int, recipients: List<String>, sendAsGroup: Boolean,
                                      body: String, attachments: List<String>) {

        Realm.getDefaultInstance().use { realm ->
            val id = (realm.where(ScheduledMessage::class.java).max("id")?.toLong() ?: -1) + 1
            val recipientsRealmList = RealmList(*recipients.toTypedArray())
            val attachmentsRealmList = RealmList(*attachments.toTypedArray())

            val message = ScheduledMessage(id, date, subId, recipientsRealmList, sendAsGroup, body, attachmentsRealmList)

            val action = "com.moez.QKSMS.SEND_SCHEDULED_MESSAGE"
            val intent = Intent(action).putExtra("id", id)
            BroadcastUtils.addClassName(context, intent, action)
            val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, date, pendingIntent)
            }

            realm.executeTransaction { realm.insertOrUpdate(message) }
        }
    }

    override fun getScheduledMessages(): RealmResults<ScheduledMessage> {
        return Realm.getDefaultInstance()
                .where(ScheduledMessage::class.java)
                .sort("date")
                .findAllAsync()
    }

    override fun getScheduledMessage(id: Long): ScheduledMessage? {
        return Realm.getDefaultInstance()
                .apply { refresh() }
                .where(ScheduledMessage::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun deleteScheduledMessage(id: Long) {
        Realm.getDefaultInstance()
                .apply { refresh() }
                .use { realm ->
                    val message = realm.where(ScheduledMessage::class.java)
                            .equalTo("id", id)
                            .findFirst()

                    realm.executeTransaction { message?.deleteFromRealm() }
                }
    }

}