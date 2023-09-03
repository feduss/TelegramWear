package com.feduss.telegramwear.viewmodel.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import com.feduss.telegram.entity.model.ChatItemModel
import com.feduss.telegramwear.business.ClientInteractor
import com.feduss.telegramwear.business.result.LoadChatResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _chatItems = MutableStateFlow<ArrayList<ChatItemModel>>(
        ArrayList()
    )

    var chatItems = _chatItems.asStateFlow()

    private var paginationLimit = 10

    private var _chatLimit = MutableStateFlow(paginationLimit)
    val chatLimit = _chatLimit.asStateFlow()

    suspend fun getChats(context: Context) {
        clientInteractor.getChats(context = context).collect {

            if (it.isNotEmpty()) {
                _chatItems.value = it
                _isLoadingChat.value = false
            }
        }
    }

    suspend fun retrieveChats(context: Context) {

        clientInteractor.retrieveChats(context = context, limit = 10).collect {
            _isLoadingChat.value = false
            _isChatLoadingFailed.value = null
            _noMoreChat.value = null
            when (it) {
                is LoadChatResult.ChatUpdated -> {

                }

                is LoadChatResult.LoadingError -> {
                    _isChatLoadingFailed.value = true
                }

                is LoadChatResult.NoMoreChat -> {
                    _noMoreChat.value = true
                }
            }
        }
    }

    fun updateChatLimit() {
        _chatLimit.value = _chatLimit.value + paginationLimit
    }
}