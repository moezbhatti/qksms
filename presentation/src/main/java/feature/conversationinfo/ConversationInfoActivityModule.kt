package feature.conversationinfo

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey
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