package feature.qkreply

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey
import javax.inject.Named

@Module
class QkReplyActivityModule {

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: QkReplyActivity): Long = activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @IntoMap
    @ViewModelKey(QkReplyViewModel::class)
    fun provideQkReplyViewModel(viewModel: QkReplyViewModel): ViewModel = viewModel

}