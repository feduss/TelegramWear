package com.feduss.telegramwear.viewmodel.chat

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.feduss.telegram.entity.LoggedUser
import com.feduss.telegram.entity.consts.ChatHistoryMessageType
import com.feduss.telegram.entity.model.ChatHistoryItemModel
import com.feduss.telegram.entity.model.ChatHistoryItemUiModel
import com.feduss.telegramwear.R
import com.feduss.telegramwear.business.ClientInteractor
import com.feduss.telegramwear.utils.getBitmapFromVectorDrawable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatHistoryViewModel @AssistedInject constructor(
    @Assisted("chatId") private val chatId: String,
    private val clientInteractor: ClientInteractor
): ViewModel() {

    //Factory
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("chatId") chatId: String
        ): ChatHistoryViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            chatId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(chatId) as T
            }
        }
    }

    private val _isLoadingChat = MutableStateFlow(true)
    var isLoadingChat = _isLoadingChat.asStateFlow()

    private val _isChatLoadingFailed = MutableStateFlow<Boolean?>(null)
    var isChatLoadingFailed = _isChatLoadingFailed.asStateFlow()

    private val _chatItems = MutableStateFlow<ArrayList<ChatHistoryItemUiModel>>(
        ArrayList()
    )

    var chatItems = _chatItems.asStateFlow()

    val fetchLimit = 100

    fun retrieveLoggedUser(): Flow<LoggedUser?> {
        return clientInteractor.getMe()
    }


    suspend fun getChatHistory(context: Context, lastMessageId: Long) {
        _isLoadingChat.value = true
        clientInteractor.getChatHistory(
            context = context,
            chatId = chatId.toLong(),
            lastMessageId = lastMessageId,
            fetchLimit = fetchLimit
        ).collect { chatHistoryModels ->

            if (chatHistoryModels.isNotEmpty()) {
                _isLoadingChat.value = false
                val newChatItems = chatHistoryModels.map { from(context, it) }// + _chatItems.value
                _chatItems.value = ArrayList(newChatItems.sortedBy { it.date })//.distinctBy { it.id })
            }

            if(chatHistoryModels.size < fetchLimit) {
                viewModelScope.launch {
                    getChatHistory(
                        context = context,
                        lastMessageId = chatHistoryModels.last().id
                    )
                }
            }
        }
    }

    private fun from(context: Context, chatHistoryItemModel: ChatHistoryItemModel): ChatHistoryItemUiModel {

        return ChatHistoryItemUiModel(
            id = chatHistoryItemModel.id,
            type = chatHistoryItemModel.type,
            quotedMessageAuthor = chatHistoryItemModel.quotedMessageAuthor,
            quotedMessageBitmap = getQuotedMessageBitmap(
                context = context,
                chatHistoryItemModel.quotedMessage
            ),
            quotedMessage = chatHistoryItemModel.quotedMessage,
            author = chatHistoryItemModel.author,
            date = chatHistoryItemModel.datetime,
            sendState = chatHistoryItemModel.sendState,
            isPinned = chatHistoryItemModel.isPinned,
            isEdited = chatHistoryItemModel.isEdited,
            canBeEdited = chatHistoryItemModel.canBeEdited,
            isRead = chatHistoryItemModel.isRead,
            forwardAuthor = chatHistoryItemModel.forwardAuthor,
            canBeForward = chatHistoryItemModel.canBeForward,
            messageDeletionOption = chatHistoryItemModel.messageDeletionOption,
            isChannelPost = chatHistoryItemModel.isChannelPost
        )
    }

    private fun getQuotedMessageBitmap(context: Context, quotedMessage: ChatHistoryMessageType?): Bitmap? {

        when (quotedMessage) {

            is ChatHistoryMessageType.Audio -> {
                return getBitmapFromVectorDrawable(context, R.drawable.ic_music)
            }

            is ChatHistoryMessageType.Photo -> {
                return getBitmapFromVectorDrawable(context, R.drawable.ic_photo)
            }

            is ChatHistoryMessageType.Video -> {
                return getBitmapFromVectorDrawable(context, R.drawable.ic_video)
            }

            is ChatHistoryMessageType.Other -> {
                return getBitmapFromVectorDrawable(context, R.drawable.ic_file)
            }

            else -> {
                return null
            }
        }
    }
}