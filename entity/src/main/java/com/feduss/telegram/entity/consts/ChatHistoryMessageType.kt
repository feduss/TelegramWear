package com.feduss.telegram.entity.consts

import android.graphics.Bitmap

sealed class ChatHistoryMessageType {

    data class Text(val message: String): ChatHistoryMessageType()

    data class Audio(
        val caption: String, val filePath: String?,
        val remoteId: String, val duration: Int
    ): ChatHistoryMessageType()

    data class Photo(
        val photoBitmap: Bitmap?, val caption: String, val filePath: String?,
        val remoteId: String, val thumbnail: Bitmap
    ): ChatHistoryMessageType()

    data class Video(
        val caption: String, val filePath: String?,
        val remoteId: String, val duration: Int, val thumbnail: Bitmap
    ): ChatHistoryMessageType()

    data class Document(
        val fileName: String, val filePath: String?,
        val remoteId: String
    ): ChatHistoryMessageType()
    data object Other: ChatHistoryMessageType()

    fun getText(): String {
        return when (this) {
            is Audio -> this.caption
            is Document -> this.fileName
            is Photo -> this.caption
            is Text -> this.message
            is Video -> this.caption
            Other -> ""
        }
    }
}
