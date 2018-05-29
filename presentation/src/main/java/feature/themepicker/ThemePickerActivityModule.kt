package feature.themepicker

import android.arch.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey
import javax.inject.Named

@Module
class ThemePickerActivityModule {

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: ThemePickerActivity): Long = activity.intent.extras?.getLong("threadId") ?: 0L

    @Provides
    @IntoMap
    @ViewModelKey(ThemePickerViewModel::class)
    fun provideThemePickerViewModel(viewModel: ThemePickerViewModel): ViewModel = viewModel

}