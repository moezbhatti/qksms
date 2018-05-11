package feature.blocked

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class BlockedActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(BlockedViewModel::class)
    fun provideBlockedViewModel(viewModel: BlockedViewModel): ViewModel = viewModel

}