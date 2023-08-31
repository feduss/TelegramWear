package com.feduss.telegramwear.view.chat

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.items
import com.feduss.telegramwear.view.component.card.chatlist.ChatItem
import com.feduss.telegramwear.view.component.card.chatlist.ChatItemSkeleton
import com.feduss.telegramwear.view.login.MainActivityViewController
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

    val isLoadingChats = viewModel.isLoadingChat.collectAsState()
    val isChatLoadingFailed = viewModel.isChatLoadingFailed.collectAsState()
    val noMoreChat = viewModel.noMoreChat.collectAsState()
    val chatItems = viewModel.chatItems.collectAsState()

    val coroutine = rememberCoroutineScope()

    LaunchedEffect(coroutine) {
        coroutine.launch {
            viewModel.getChatList(
                context = activity
            )
        }
    }



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
            items(chatItems.value.toList()) {
                ChatItem(
                    image = it.image,
                    personName = it.personName,
                    lastMessageImage = it.lastMessageImage,
                    lastMessage = it.lastMessage,
                    onCardClick = {

                    }
                )
            }
        }
    }
}