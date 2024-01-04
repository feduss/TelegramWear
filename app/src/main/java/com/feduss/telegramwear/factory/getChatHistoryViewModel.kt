package com.feduss.telegramwear.factory

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feduss.telegramwear.hilt.ViewModelFactory
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.viewmodel.chat.ChatHistoryViewModel
import dagger.hilt.android.EntryPointAccessors

@Composable
fun getChatHistoryViewModel(
    activity: MainActivityViewController,
    chatId: String
): ChatHistoryViewModel = viewModel(
    factory = ChatHistoryViewModel.provideFactory(
        assistedFactory = EntryPointAccessors.fromActivity(
            activity,
            ViewModelFactory::class.java
        ).chatHistoryViewModelFactory(),
        chatId = chatId
    )
)