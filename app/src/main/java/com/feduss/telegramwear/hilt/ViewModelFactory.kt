package com.feduss.telegramwear.hilt

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactory {
    /*fun directionsViewModelFactory(): DirectionsViewModel.Factory*/
}