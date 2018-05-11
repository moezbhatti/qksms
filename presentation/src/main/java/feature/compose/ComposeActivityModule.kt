package feature.compose

import android.arch.lifecycle.ViewModel
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class ComposeActivityModule {

    @Provides
    fun provideIntent(activity: ComposeActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(ComposeViewModel::class)
    fun provideComposeViewModel(viewModel: ComposeViewModel): ViewModel = viewModel

}