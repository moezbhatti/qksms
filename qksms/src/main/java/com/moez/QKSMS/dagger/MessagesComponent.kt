package com.moez.QKSMS.dagger

import com.moez.QKSMS.ui.messages.MessageListActivity
import dagger.Component

@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(MessagesModule::class))
interface MessagesComponent {

    fun inject(activity: MessageListActivity)

}