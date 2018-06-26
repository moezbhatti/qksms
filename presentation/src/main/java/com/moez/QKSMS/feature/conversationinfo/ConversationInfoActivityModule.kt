package com.moez.QKSMS.feature.conversationinfo

import androidx.lifecycle.ViewModel
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Named

@Module
class ConversationInfoActivityModule {

    @Provides
    @Named("threadId")
    fun provideIntent(activity: ConversationInfoActivity): Long = activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @IntoMap
    @ViewModelKey(ConversationInfoViewModel::class)
    fun provideConversationInfoViewModel(viewModel: ConversationInfoViewModel): ViewModel = viewModel

}