package com.feduss.telegram.entity.model

import com.feduss.telegram.entity.consts.ChatHistoryMessageType
import com.feduss.telegram.entity.consts.MessageDeletionOption
import com.feduss.telegram.entity.consts.MessageSendState
import java.time.LocalDateTime

class ChatHistoryItemModel(
    val id: Long,
    val type: ChatHistoryMessageType,
    val quotedMessageAuthor: MessageAuthor?, val quotedMessage: ChatHistoryMessageType?,
    val author: MessageAuthor?, val datetime: LocalDateTime,
    val sendState: MessageSendState?, val isPinned: Boolean,
    val isEdited: Boolean, val canBeEdited: Boolean,
    val isRead: Boolean,
    val forwardAuthor: String?, val canBeForward: Boolean,
    val messageDeletionOption: MessageDeletionOption,
    val isChannelPost: Boolean
) {
}

class MessageAuthor(val id: Long, val name: String)