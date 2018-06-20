package com.moez.QKSMS.feature.notificationprefs

import android.arch.lifecycle.ViewModel
import android.content.Intent
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class NotificationPrefsActivityModule {

    @Provides
    fun provideIntent(activity: NotificationPrefsActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(NotificationPrefsViewModel::class)
    fun provideNotificationPrefsViewModel(viewModel: NotificationPrefsViewModel): ViewModel = viewModel

}