package feature.settings.about

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class AboutActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    fun provideAboutViewModel(viewModel: AboutViewModel): ViewModel = viewModel

}