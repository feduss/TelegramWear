package com.feduss.telegramwear.hilt

import com.feduss.telegramwear.viewmodel.chat.ChatHistoryViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactory {
    fun chatHistoryViewModelFactory(): ChatHistoryViewModel.Factory
}