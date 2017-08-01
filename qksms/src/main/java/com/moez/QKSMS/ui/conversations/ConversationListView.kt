package com.moez.QKSMS.ui.conversations

import com.hannesdorfmann.mosby.mvp.MvpView
import com.moez.QKSMS.model.Conversation
import io.realm.RealmResults

interface ConversationListView : MvpView {

    fun setConversations(conversations: RealmResults<Conversation>)

}