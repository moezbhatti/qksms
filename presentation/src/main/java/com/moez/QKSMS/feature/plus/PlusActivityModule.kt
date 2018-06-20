package com.moez.QKSMS.feature.plus

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class PlusActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(PlusViewModel::class)
    fun providePlusViewModel(viewModel: PlusViewModel): ViewModel = viewModel

}