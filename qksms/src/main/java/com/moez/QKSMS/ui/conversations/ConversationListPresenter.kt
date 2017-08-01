package com.moez.QKSMS.ui.conversations

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.moez.QKSMS.model.Conversation
import io.realm.Realm

class ConversationListPresenter : MvpBasePresenter<ConversationListView>() {

    override fun attachView(view: ConversationListView) {
        super.attachView(view)

        val realm = Realm.getDefaultInstance()
        val realmResults = realm.where(Conversation::class.java).findAll()

        view.setConversations(realmResults)
    }

}