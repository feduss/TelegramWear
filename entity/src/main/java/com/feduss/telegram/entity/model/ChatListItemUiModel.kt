package com.feduss.telegram.entity.model

import android.graphics.Bitmap

class ChatListItemUiModel(
    val id: Long, val image: Bitmap, var personName: String, val lastMessageImage: Bitmap?,
    val lastMessageId: String, val lastMessage: String, val lastMessageDate: String,
    val unreadMessagesCount: Int,
    val isPinned: Boolean, val isMuted: Boolean,
    val isOnline: Boolean, val hasOnlineStatus: Boolean,
) {
}