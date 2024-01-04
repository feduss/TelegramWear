package com.feduss.telegram.entity.consts

sealed class MessageDeletionOption {
    data object OnlyForMe: MessageDeletionOption()
    data object ForAll: MessageDeletionOption()
    data object None: MessageDeletionOption()
}
