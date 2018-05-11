package feature.settings

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class SettingsActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun provideSettingsViewModel(viewModel: SettingsViewModel): ViewModel = viewModel

}