package com.moez.QKSMS.ui.conversations

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.moez.QKSMS.model.Conversation
import io.realm.Realm

class ConversationPresenter : MvpBasePresenter<ConversationView>() {

    override fun attachView(view: ConversationView?) {
        super.attachView(view)

        loadConversations()
    }

    fun loadConversations() {
        val realm = Realm.getDefaultInstance()
        val realmResults = realm.where(Conversation::class.java).findAll()

        view?.setConversations(realmResults)
    }


}