package com.moez.QKSMS.ui.messages

import com.hannesdorfmann.mosby.mvp.MvpView
import com.moez.QKSMS.model.Message
import io.realm.RealmResults

interface MessageListView : MvpView {

    fun setMessages(messages: RealmResults<Message>)

}
