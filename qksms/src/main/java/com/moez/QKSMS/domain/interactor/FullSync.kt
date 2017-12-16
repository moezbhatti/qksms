package com.moez.QKSMS.domain.interactor

import android.content.Context
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import com.moez.QKSMS.data.mapper.CursorToRecipient
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.model.Recipient
import com.moez.QKSMS.data.model.SyncLog
import io.reactivex.Flowable
import io.realm.Realm
import javax.inject.Inject

class FullSync @Inject constructor(
        context: Context,
        cursorToConversation: CursorToConversation,
        cursorToMessage: CursorToMessage,
        cursorToRecipient: CursorToRecipient)
    : PartialSync(context, cursorToConversation, cursorToMessage, cursorToRecipient) {

    override fun buildObservable(params: Unit): Flowable<Long> {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            realm.delete(Conversation::class.java)
            realm.delete(Message::class.java)
            realm.delete(Recipient::class.java)
            realm.delete(SyncLog::class.java)
        }
        realm.close()

        return super.buildObservable(params)
    }

}