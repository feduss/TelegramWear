package com.feduss.telegram.entity.consts

sealed class LastMessageType {
    data class Draft(val text: String) : LastMessageType()
    data class Text(val text: String) : LastMessageType()
    data class Document(val filename: String) : LastMessageType()
    data object Photo: LastMessageType()
    data object Video: LastMessageType()
    data object VoiceNote: LastMessageType()
    data object Animation: LastMessageType()
    data class Sticker(val emoji: String) : LastMessageType()
    data class AnimatedEmoji(val emoji: String) : LastMessageType()
    data object Other: LastMessageType()
}
