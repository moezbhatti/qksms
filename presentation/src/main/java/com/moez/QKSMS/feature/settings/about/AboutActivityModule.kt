package com.moez.QKSMS.feature.settings.about

import androidx.lifecycle.ViewModel
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class AboutActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    fun provideAboutViewModel(viewModel: AboutViewModel): ViewModel = viewModel

}