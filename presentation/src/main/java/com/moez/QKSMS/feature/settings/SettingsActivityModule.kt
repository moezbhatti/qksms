package com.moez.QKSMS.feature.settings

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class SettingsActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun provideSettingsViewModel(viewModel: SettingsViewModel): ViewModel = viewModel

}