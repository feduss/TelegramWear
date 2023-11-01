package com.feduss.telegram.entity.model

import com.feduss.telegram.entity.consts.LastMessageType

class ChatListItemModel(
    val id: Long, val imagePath: String?, var personName: String?,
    val lastMessageId: String, val lastMessageType: LastMessageType, val lastMessageDateTimestamp: Long?,
    val unreadMessagesCount: Int,
    val isPinned: Boolean, val isMuted: Boolean,
    val isOnline: Boolean, val hasOnlineStatus: Boolean,
) {
}