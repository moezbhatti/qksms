package feature.plus

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class PlusActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(PlusViewModel::class)
    fun providePlusViewModel(viewModel: PlusViewModel): ViewModel = viewModel

}