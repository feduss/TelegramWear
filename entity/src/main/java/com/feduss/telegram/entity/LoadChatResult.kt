package com.feduss.telegram.entity

sealed class LoadChatResult {
    data object ChatUpdated: LoadChatResult()
    data object NoMoreChat: LoadChatResult()
    data object LoadingError: LoadChatResult()
}
