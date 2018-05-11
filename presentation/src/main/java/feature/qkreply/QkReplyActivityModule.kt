package feature.qkreply

import android.arch.lifecycle.ViewModel
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class QkReplyActivityModule {

    @Provides
    fun provideIntent(activity: QkReplyActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(QkReplyViewModel::class)
    fun provideQkReplyViewModel(viewModel: QkReplyViewModel): ViewModel = viewModel

}