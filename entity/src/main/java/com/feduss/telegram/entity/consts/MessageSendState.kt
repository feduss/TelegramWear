package com.feduss.telegram.entity.consts

sealed class MessageSendState {
    data object Pending: MessageSendState()
    data object Failed: MessageSendState()
}
