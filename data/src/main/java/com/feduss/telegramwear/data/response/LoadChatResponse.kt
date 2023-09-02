package com.feduss.telegramwear.data.response

import org.drinkless.td.libcore.telegram.TdApi

sealed class LoadChatResponse {
    data class ChatList(val chats: ArrayList<TdApi.Chat>, val ids: List<Long>): LoadChatResponse()
    data object NoMoreChat: LoadChatResponse()
    data object LoadingError: LoadChatResponse()
}
