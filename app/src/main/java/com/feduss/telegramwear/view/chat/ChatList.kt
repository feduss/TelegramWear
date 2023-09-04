package com.feduss.telegramwear.view.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.feduss.telegramwear.R
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.view.component.button.TextButton
import com.feduss.telegramwear.view.component.card.chatlist.ChatItem
import com.feduss.telegramwear.view.component.card.chatlist.ChatItemSkeleton
import com.feduss.telegramwear.viewmodel.chat.ChatListViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ChatList(
    activity: MainActivityViewController,
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: ChatListViewModel = hiltViewModel()
) {

    val isLoadingChats = viewModel.isLoadingChat.collectAsState()
    val isUpdatingChatLimit = viewModel.isUpdatingChatLimit.collectAsState()
    val isChatLoadingFailed = viewModel.isChatLoadingFailed.collectAsState()
    val chatItems = viewModel.chatItems.collectAsState()
    val chatLimit = viewModel.chatLimit.collectAsState()
    val limitedChatItems = chatItems.value.toList().take(chatLimit.value)

    val coroutine = rememberCoroutineScope()

    LaunchedEffect(coroutine) {
        coroutine.launch(Dispatchers.IO) {
            viewModel.getChats(context = activity)
        }
    }

    if (isChatLoadingFailed.value == true) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.chat_list_loading_error),
                textAlign = TextAlign.Center
            )
        }
    } else {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            columnState = columnState
        ){

            if (isLoadingChats.value == true) {
                items(3) {
                    ChatItemSkeleton()
                }
            } else {
                items(limitedChatItems) {
                    ChatItem                    (
                        model = it,
                        onCardClick = {

                        }
                    )
                }

                if (isUpdatingChatLimit.value == false && chatLimit.value < chatItems.value.size) {
                    item {
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(32.dp),
                            enabled = true,
                            onClick = {
                                coroutine.launch {
                                    viewModel.updateChatLimit()
                                }
                            },
                            title = stringResource(R.string.chat_list_load_more)
                        )
                    }
                }

                if (isUpdatingChatLimit.value == true) {
                    items(3) {
                        ChatItemSkeleton()
                    }
                }
            }
        }
    }
}