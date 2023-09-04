package com.feduss.telegramwear.viewmodel.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feduss.telegram.entity.model.ChatItemModel
import com.feduss.telegramwear.business.ClientInteractor
import com.feduss.telegramwear.business.result.LoadChatResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
): ViewModel() {

    private val _isLoadingChat = MutableStateFlow(true)
    var isLoadingChat = _isLoadingChat.asStateFlow()

    private val _isChatLoadingFailed = MutableStateFlow<Boolean?>(null)
    var isChatLoadingFailed = _isChatLoadingFailed.asStateFlow()

    private val _isUpdatingChatLimit = MutableStateFlow<Boolean>(false)
    var isUpdatingChatLimit = _isUpdatingChatLimit.asStateFlow()

    private val _chatItems = MutableStateFlow<ArrayList<ChatItemModel>>(
        ArrayList()
    )

    var chatItems = _chatItems.asStateFlow()

    private var paginationLimit = 10

    private var _chatLimit = MutableStateFlow(paginationLimit)
    val chatLimit = _chatLimit.asStateFlow()

    init {
        retrieveChats()
    }

    private fun retrieveChats() {
        viewModelScope.launch {
            clientInteractor.retrieveChats(limit = _chatLimit.value).collect {
                _isChatLoadingFailed.value = null
                when (it) {
                    is LoadChatResult.LoadingError -> {
                        _isChatLoadingFailed.value = true
                    }

                    else -> {}
                }
            }
        }
    }

    suspend fun getChats(context: Context) {
        clientInteractor.getChats(context = context).collect {

            //Log.i("LogTest: ", "Data ready with chat size ${it.size}")
            if (it.isNotEmpty()) {
                _isLoadingChat.value = false
                _chatItems.value = it
            }
        }
    }

    fun updateChatLimit() {
        _chatLimit.value = _chatLimit.value + paginationLimit
        retrieveChats()
    }
}