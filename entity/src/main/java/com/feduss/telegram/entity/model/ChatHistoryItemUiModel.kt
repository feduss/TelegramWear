package com.feduss.telegram.entity.model

import android.graphics.Bitmap
import com.feduss.telegram.entity.consts.ChatHistoryMessageType
import com.feduss.telegram.entity.consts.MessageDeletionOption
import com.feduss.telegram.entity.consts.MessageSendState
import java.time.LocalDateTime

class ChatHistoryItemUiModel(
    val id: Long,
    val type: ChatHistoryMessageType,
    val quotedMessageAuthor: MessageAuthor?,
    val quotedMessageBitmap: Bitmap?, val quotedMessage: ChatHistoryMessageType?,
    val author: MessageAuthor?, val date: LocalDateTime,
    val sendState: MessageSendState?, val isPinned: Boolean,
    val isEdited: Boolean, canBeEdited: Boolean,
    val isRead: Boolean,
    val forwardAuthor: String?, canBeForward: Boolean,
    val messageDeletionOption: MessageDeletionOption,
    val isChannelPost: Boolean
) {
}
