package feature.notificationprefs

import android.arch.lifecycle.ViewModel
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import injection.ViewModelKey

@Module
class NotificationPrefsActivityModule {

    @Provides
    fun provideIntent(activity: NotificationPrefsActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(NotificationPrefsViewModel::class)
    fun provideNotificationPrefsViewModel(viewModel: NotificationPrefsViewModel): ViewModel = viewModel

}