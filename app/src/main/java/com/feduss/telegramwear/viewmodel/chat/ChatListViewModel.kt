package com.feduss.telegramwear.viewmodel.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.telegram.entity.model.ChatItemModel
import com.feduss.telegramwear.business.ClientInteractor
import com.feduss.telegramwear.business.result.LoadChatResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
): ViewModel() {

    private val _isLoadingChat = MutableStateFlow(true)
    var isLoadingChat = _isLoadingChat.asStateFlow()

    private val _isChatLoadingFailed = MutableStateFlow<Boolean?>(null)
    var isChatLoadingFailed = _isChatLoadingFailed.asStateFlow()

    private val _noMoreChat = MutableStateFlow<Boolean?>(null)
    var noMoreChat = _noMoreChat.asStateFlow()

    private val _chatItems = MutableStateFlow<List<ChatItemModel>>(listOf())
    var chatItems = _chatItems.asStateFlow()

    suspend fun getChatList(context: Context) {
        _isLoadingChat.value = true
        _isChatLoadingFailed.value = null
        _noMoreChat.value = null

        clientInteractor.retrieveChats(context = context, limit = 100).collect() {
            when (it) {
                is LoadChatResult.ChatList -> {
                    _chatItems.value = it.chats
                }

                is LoadChatResult.LoadingError -> {
                    _isChatLoadingFailed.value = true
                }

                is LoadChatResult.NoMoreChat -> {
                    _noMoreChat.value = true
                }
            }

            _isLoadingChat.value = false
        }
    }
}