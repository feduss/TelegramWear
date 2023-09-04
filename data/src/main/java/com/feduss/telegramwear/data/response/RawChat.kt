package com.feduss.telegramwear.data.response

import org.drinkless.td.libcore.telegram.TdApi

class RawChat(
    val chats: List<TdApi.Chat>,
    val usersStatus: Map<Long, Boolean?>) {

}