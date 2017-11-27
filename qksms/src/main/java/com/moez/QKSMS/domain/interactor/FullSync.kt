package com.moez.QKSMS.domain.interactor

import android.content.Context
import com.moez.QKSMS.data.mapper.CursorToConversation
import com.moez.QKSMS.data.mapper.CursorToMessage
import io.reactivex.Flowable
import io.realm.Realm
import javax.inject.Inject

class FullSync @Inject constructor(
        context: Context,
        cursorToConversation: CursorToConversation,
        cursorToMessage: CursorToMessage)
    : PartialSync(context, cursorToConversation, cursorToMessage) {

    override fun buildObservable(params: Unit): Flowable<Long> {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm.deleteAll() }
        realm.close()

        return super.buildObservable(params)
    }

}