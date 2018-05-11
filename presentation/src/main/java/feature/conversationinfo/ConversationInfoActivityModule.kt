package feature.conversationinfo

import android.arch.lifecycle.ViewModel
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class ConversationInfoActivityModule {

    @Provides
    fun provideIntent(activity: ConversationInfoActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(ConversationInfoViewModel::class)
    fun provideConversationInfoViewModel(viewModel: ConversationInfoViewModel): ViewModel = viewModel

}