package com.feduss.telegram.entity.consts

sealed class Params(val name: String) {
    data object ChatId: Params("chatId")
}
