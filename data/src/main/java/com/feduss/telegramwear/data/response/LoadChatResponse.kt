package com.feduss.telegramwear.data.response

import org.drinkless.td.libcore.telegram.TdApi
import java.util.concurrent.ConcurrentHashMap

sealed class LoadChatResponse {
    data object ChatUpdated: LoadChatResponse()
    data object NoMoreChat: LoadChatResponse()
    data object LoadingError: LoadChatResponse()
}
