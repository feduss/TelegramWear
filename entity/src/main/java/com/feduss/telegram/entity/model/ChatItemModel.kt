package com.feduss.telegram.entity.model

import android.graphics.Bitmap

class ChatItemModel(
    val image: Bitmap, val personName: String, val lastMessageImage: Bitmap?,
    val lastMessage: String, val lastMessageDate: String, val unreadMessagesCount: Int, val isPinned: Boolean, val isMuted: Boolean,
    val isOnline: Boolean
) {
}