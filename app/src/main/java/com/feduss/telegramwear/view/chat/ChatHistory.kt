package com.feduss.telegramwear.view.chat

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.view.component.card.chat.history.ChatHistoryItem
import com.feduss.telegramwear.view.component.card.chat.history.ChatHistoryItemSkeleton
import com.feduss.telegramwear.viewmodel.chat.ChatHistoryViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalHorologistApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatHistory(
    activity: MainActivityViewController,
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: ChatHistoryViewModel,
    profilePhoto: Bitmap,
    profileName: String
) {

    val isLoadingChats = viewModel.isLoadingChat.collectAsState()
    val isChatLoadingFailed = viewModel.isChatLoadingFailed.collectAsState()
    val chatItems = viewModel.chatItems.collectAsState()
    val loggedUser = viewModel.retrieveLoggedUser().collectAsState(null)

    val coroutine = rememberCoroutineScope()

    var firstTimeScroll by remember {
        mutableStateOf(true)
    }

    var requestMoreChat by remember {
        mutableStateOf(true)
    }

    val topReached by remember {
        derivedStateOf {
            !columnState.state.canScrollBackward //TODO: to impl
        }
    }

    if (isLoadingChats.value == false && topReached) {
        requestMoreChat = true
    }


    LaunchedEffect(requestMoreChat) {
        if (requestMoreChat) {

            coroutine.launch {
                viewModel.getChatHistory(
                    context = activity,
                    lastMessageId = chatItems.value.firstOrNull()?.id ?: 0L
                )
                requestMoreChat = false
                Log.i("LogTest: ", "nuove chat")
            }
        }
    }

    LaunchedEffect(isLoadingChats.value) {
        if (firstTimeScroll) {
            coroutine.launch {
                if (isLoadingChats.value == true) {
                    columnState.state.scrollToItem(2)
                } else if (isLoadingChats.value == false) {
                    val estimatedIndex = columnState.state.layoutInfo.totalItemsCount - 1
                    val index = if (estimatedIndex > 0) {
                        estimatedIndex
                    } else {
                        0
                    }

                    val offset: Int = columnState.state.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0
                    columnState.state.scrollToItem(
                        index,
                        offset / 2 //TODO: to test
                    )
                    firstTimeScroll = false
                }
            }
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
        ) {

            if (isLoadingChats.value == true) {
                items(3) {
                    ChatHistoryItemSkeleton(
                        horizontalAlignment = if (it % 2 == 0) {
                            Alignment.Start
                        } else {
                            Alignment.End
                        }
                    )
                }
            }
            
            var prevDate: String? = null

            items(chatItems.value) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val itemDate = it.date.format(
                        DateTimeFormatter.ofPattern("dd MMM u")
                    )

                    if(prevDate == null || prevDate != itemDate) {
                        prevDate = itemDate

                        Text(text = prevDate.toString())
                    }

                    ChatHistoryItem(
                        model = it,
                        horizontalAlignment =
                        //TODO: to test
                        if (it.author?.id == loggedUser.value?.id) {
                            Alignment.Start
                        }
                        else {
                            Alignment.End
                        },
                        cardTintColor = if (it.author?.id == loggedUser.value?.id) {
                            AppColors.TelegramBlue.toColor()
                        } else {
                            Color.DarkGray
                        },
                        onCardClick = {

                        },
                        onDownloadTapped = { remoteId ->

                        }
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            4.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        val backgroundColor = AppColors.TelegramBlue.toColor()
                        val tintColor = Color.White

                        //Audio answer button
                        CompactButton(
                            modifier = Modifier
                                .width(48.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = backgroundColor,
                                    shape = CircleShape
                                ),
                            colors = ButtonDefaults.primaryButtonColors(
                                backgroundColor,
                                tintColor
                            ),
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_audio_message),
                                    contentDescription = "Audio answer icon button",
                                    tint = Color.Black
                                )
                            },
                            onClick = {
                                if (isLoadingChats.value == false) {
                                    //TODO: to impl
                                }
                            }
                        )

                        //Text answer button
                        CompactButton(
                            modifier = Modifier
                                .width(48.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = backgroundColor,
                                    shape = CircleShape
                                ),
                            colors = ButtonDefaults.primaryButtonColors(
                                backgroundColor,
                                tintColor
                            ),
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_keyboard),
                                    contentDescription = "Text answer icon button",
                                    tint = Color.Black
                                )
                            },
                            onClick = {
                                if (isLoadingChats.value == false) {
                                    //TODO: to impl
                                }
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            bitmap = profilePhoto.asImageBitmap(),
                            contentDescription = "$profileName profile picture"
                        )

                        Text(
                            maxLines = 1,
                            modifier = Modifier
                                .basicMarquee(),
                            text = profileName,
                            fontSize = TextUnit(12f, TextUnitType.Sp),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}