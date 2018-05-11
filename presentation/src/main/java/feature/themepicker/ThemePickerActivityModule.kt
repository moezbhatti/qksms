package feature.themepicker

import android.arch.lifecycle.ViewModel
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class ThemePickerActivityModule {

    @Provides
    fun provideIntent(activity: ThemePickerActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(ThemePickerViewModel::class)
    fun provideThemePickerViewModel(viewModel: ThemePickerViewModel): ViewModel = viewModel

}