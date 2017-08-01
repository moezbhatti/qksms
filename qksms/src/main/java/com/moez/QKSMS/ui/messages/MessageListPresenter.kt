package com.moez.QKSMS.ui.messages

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.moez.QKSMS.model.Message
import io.realm.Realm

class MessageListPresenter : MvpBasePresenter<MessageListView>() {

    var threadId: Long? = null
        set(value) {
            field = value
        }

    override fun attachView(view: MessageListView?) {
        super.attachView(view)

        val realmResults = Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAll()

        view?.setMessages(realmResults)
    }

}