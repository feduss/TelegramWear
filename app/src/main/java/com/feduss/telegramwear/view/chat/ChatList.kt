package com.feduss.telegramwear.view.chat

import android.util.Log
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
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.R
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.view.component.button.TextButton
import com.feduss.telegramwear.view.component.card.chat.list.ChatListItem
import com.feduss.telegramwear.view.component.card.chat.list.ChatListItemSkeleton
import com.feduss.telegramwear.viewmodel.chat.ChatListViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ChatList(
    activity: MainActivityViewController,
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: ChatListViewModel = hiltViewModel()
) {

    val isUpdatingChatLimit = viewModel.isUpdatingChatLimit.collectAsState()
    val isChatLoadingFailed = viewModel.isChatLoadingFailed.collectAsState()
    val chatItems = viewModel.chats.collectAsState()
    val firstTimeLoading = viewModel.firstTimeLoading.collectAsState()
    val chatLimit = viewModel.chatLimit.collectAsState()
    val limitedChatItems = chatItems.value.toList().take(chatLimit.value)
    val loggedUser = viewModel.retrieveLoggedUser().collectAsState(null)

    val coroutine = rememberCoroutineScope()

    LaunchedEffect(firstTimeLoading) {

        Log.i("LogTest: ", "Loading chats list")

        coroutine.launch {
            viewModel.getChatModels(context = activity)
        }

        coroutine.launch {
            viewModel.requestChats()
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

            if (chatItems.value.isEmpty()) {
                items(3) {
                    ChatListItemSkeleton()
                }
            } else {
                items(limitedChatItems) {

                    if (it.id == loggedUser.value?.id) {
                        it.personName = "Messaggi salvati"
                    }

                    ChatListItem (
                        model = it,
                        onCardClick = {
                            val args = listOf(it.id.toString())

                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("profilePhoto", it.image)
                                set("profileName", it.personName)
                                set("lastMessageId", it.lastMessageId)
                            }

                            navController.navigate(Section.ChatHistory.withArgs(args))
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
                        ChatListItemSkeleton()
                    }
                }
            }
        }
    }
}