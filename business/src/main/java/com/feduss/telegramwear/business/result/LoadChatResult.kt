package com.feduss.telegramwear.business.result

import com.feduss.telegram.entity.model.ChatItemModel

sealed class LoadChatResult {
    data object ChatUpdated: LoadChatResult()
    data object NoMoreChat: LoadChatResult()
    data object LoadingError: LoadChatResult()
}
