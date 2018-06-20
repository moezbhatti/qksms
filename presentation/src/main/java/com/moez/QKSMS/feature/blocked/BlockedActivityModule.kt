package com.moez.QKSMS.feature.blocked

import android.arch.lifecycle.ViewModel
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class BlockedActivityModule {

    @Provides
    @IntoMap
    @ViewModelKey(BlockedViewModel::class)
    fun provideBlockedViewModel(viewModel: BlockedViewModel): ViewModel = viewModel

}