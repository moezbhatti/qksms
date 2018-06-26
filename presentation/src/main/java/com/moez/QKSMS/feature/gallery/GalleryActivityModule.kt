package com.moez.QKSMS.feature.gallery

import androidx.lifecycle.ViewModel
import android.content.Intent
import com.moez.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class GalleryActivityModule {

    @Provides
    fun provideIntent(activity: GalleryActivity): Intent = activity.intent

    @Provides
    @IntoMap
    @ViewModelKey(GalleryViewModel::class)
    fun provideGalleryViewModel(viewModel: GalleryViewModel): ViewModel = viewModel

}