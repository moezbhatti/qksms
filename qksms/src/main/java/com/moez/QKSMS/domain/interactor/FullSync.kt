package com.moez.QKSMS.domain.interactor

import android.content.Context
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import com.moez.QKSMS.data.mapper.CursorToRecipient
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
        realm.executeTransaction { realm.deleteAll() }
        realm.close()

        return super.buildObservable(params)
    }

}