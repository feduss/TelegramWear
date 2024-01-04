package com.feduss.telegramwear.viewmodel.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.feduss.telegram.entity.LoadChatResult
import com.feduss.telegram.entity.LoggedUser
import com.feduss.telegram.entity.consts.LastMessageType
import com.feduss.telegram.entity.model.ChatListItemModel
import com.feduss.telegram.entity.model.ChatListItemUiModel
import com.feduss.telegramwear.R
import com.feduss.telegramwear.business.ClientInteractor
import com.feduss.telegramwear.utils.getBitmapFromVectorDrawable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject


@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor,
    private val state: SavedStateHandle
): ViewModel() {

    val chats = state.getStateFlow<List<ChatListItemUiModel>>("chatsList", listOf())

    val firstTimeLoading = state.getStateFlow("firstTimeLoading", true)

    val isChatLoadingFailed = state.getStateFlow<Boolean?>("isChatLoadingFailed", null)

    val isUpdatingChatLimit = state.getStateFlow("isUpdatingChatLimit", false)

    private var paginationLimit = 10

    val chatLimit = state.getStateFlow("chatLimit", paginationLimit)

    fun retrieveLoggedUser(): Flow<LoggedUser?> {
        return clientInteractor.getMe()
    }

    suspend fun requestChats() {
        clientInteractor.requestChats(limit = chatLimit.value).collect {

            state["isChatLoadingFailed"] = null
            when (it) {
                is LoadChatResult.LoadingError -> {
                    state["isChatLoadingFailed"] = true
                }

                else -> {}
            }
        }
    }

    suspend fun getChatModels(context: Context) {

        state["firstTimeLoading"] = false

        return clientInteractor.getChatModels(context = context).collect { chats ->
            state["chatsList"] = chats.map { chatListItemModel ->
                from(
                    chatListItemModel,
                    context = context
                )
            }
            .sortedByDescending { it.orderId }
        }
    }

    fun updateChatLimit() {
        state["chatLimit"] = chatLimit.value + paginationLimit
    }

    private fun from(chatListItemModel: ChatListItemModel, context: Context): ChatListItemUiModel {

        val personName = chatListItemModel.personName ?: context.getString(R.string.chat_title_deleted_account)

        val (lastMessageImage: Bitmap?, lastMessage) = getLastMessage(chatListItemModel.lastMessageType, context)

        val lastMessageDate = getLastMessageDate(context, chatListItemModel.lastMessageDateTimestamp)

        return ChatListItemUiModel(
            id = chatListItemModel.id,
            image = getProfilePhoto(
                chatId = chatListItemModel.id,
                imagePath = chatListItemModel.imagePath,
                personName = personName
            ),
            personName = personName ,
            lastMessageImage = lastMessageImage,
            lastMessageId = chatListItemModel.lastMessageId,
            lastMessage = lastMessage,
            lastMessageDate = lastMessageDate,
            unreadMessagesCount = chatListItemModel.unreadMessagesCount,
            isPinned = chatListItemModel.isPinned,
            isMuted = chatListItemModel.isMuted,
            isOnline = chatListItemModel.isOnline,
            hasOnlineStatus = chatListItemModel.hasOnlineStatus,
            orderId = chatListItemModel.orderId
        )
    }

    private fun getProfilePhoto(chatId: Long, imagePath: String?, personName: String): Bitmap {
        val image: Bitmap = try {

            if (File(imagePath ?: "").exists()) {
                BitmapFactory.decodeFile(imagePath)
            } else {
                getDefaultImageWithText(personName, chatId)
            }
        } catch(e: Exception) {
            getDefaultImageWithText(personName,chatId)
        }

        return image
    }

    private fun getDefaultImageWithText(personName: String, chatId: Long): Bitmap {

        val colors = listOf(
            Color.Red,
            Color.Green,
            Color.Yellow,
            Color.Blue,
            Color(0x80008000), //Purple
            Color(0xFFC0CB00), //Pink
            Color.Blue,
            Color(0xFFA50000) //Orange
        )

        var id = (chatId.toString().replace("-100", "-")).toLong()
        if (id < 0) id = -id;
        val randomColor = colors[intArrayOf(0, 7, 4, 1, 6, 3, 5)[(id.mod(7))]];

        val image = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val backgroundColor: Int = android.graphics.Color.argb(
            randomColor.alpha,
            randomColor.red,
            randomColor.green,
            randomColor.blue
        )
        val backgroundPaint = Paint()
        backgroundPaint.color = backgroundColor

        canvas.drawColor(backgroundColor)

        val whiteColor = Color.White

        val textColor: Int = android.graphics.Color.argb(
            whiteColor.alpha,
            whiteColor.red,
            whiteColor.green,
            whiteColor.blue
        )

        val textPaint = Paint()
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 80f

        val splittedStrings = personName.split(" ")
        val text = if (splittedStrings.count() > 1) {
            val firstChar = splittedStrings[0].firstOrNull() ?: ' '
            val secondChar = splittedStrings[1].firstOrNull() ?: ' '
            "${firstChar.uppercase()}${secondChar.uppercase()}"
        } else {
            val char = personName.firstOrNull() ?: ' '
            char.uppercase()
        }

        canvas.drawText(
            text,
            canvas.width / 2f,
            (canvas.height / 2f) - (( textPaint.descent() + textPaint.ascent()) / 2),
            textPaint
        )

        return image
    }

    private fun getLastMessage(lastMessageType: LastMessageType, context: Context): Pair<Bitmap?, String> {
        var lastMessageImage: Bitmap? = null
        val lastMessage: String

        when (lastMessageType) {

            is LastMessageType.Draft -> {
                lastMessage = context.getString(R.string.last_message_draft, lastMessageType.text)
            }

            is LastMessageType.Text -> {
                lastMessage = lastMessageType.text
            }

            is LastMessageType.Document -> {
                lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_file)
                lastMessage = lastMessageType.filename
            }

            is LastMessageType.Photo -> {
                lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_photo)
                lastMessage = "Foto"
            }

            is LastMessageType.Video -> {
                lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_video)
                lastMessage = "Video"
            }

            is LastMessageType.VoiceNote -> {
                lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_music)
                lastMessage = "Audio"
            }

            is LastMessageType.Animation -> {
                lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_gif)
                lastMessage = "GIF"
            }

            is LastMessageType.Sticker -> {
                lastMessage = "${lastMessageType.emoji} Sticker"
            }

            is LastMessageType.AnimatedEmoji -> {
                lastMessage = "${lastMessageType.emoji} Sticker"
            }

            else -> {
                lastMessage = context.getString(R.string.last_message_unsupported_message)
            }
        }

        return Pair(lastMessageImage, lastMessage)
    }

    private fun getLastMessageDate(context: Context, timestamp: Long?): String {
        val rawLastMessageDate = Instant.ofEpochSecond(
            timestamp ?: 0L
        )

        val timezonedLastMessageDate = ZonedDateTime.ofInstant(rawLastMessageDate, ZoneId.systemDefault())
        val timezonedNow = ZonedDateTime.now(ZoneId.systemDefault())

        val diff: Duration = Duration.between(
            timezonedLastMessageDate,
            timezonedNow
        )

        val lastMessageDate: String = if (diff.toMinutes() < 1L) {
            context.getString(R.string.last_message_date_less_than_minute)
        } else if (diff.toMinutes() == 1L) {
            context.getString(R.string.last_message_date_one_minute)
        } else if (diff.toMinutes() < 60L) {
            context.getString(R.string.last_message_date_n_minutes, diff.toMinutes().toString())
        } else if (diff.toHours() == 1L) {
            context.getString(R.string.last_message_date_one_hour)
        } else if (diff.toHours() < 24L) {
            context.getString(R.string.last_message_date_n_hours, diff.toHours().toString())
        } else if (diff.toDays() == 1L) {
            context.getString(R.string.last_message_date_one_day)
        } else {
            context.getString(R.string.last_message_date_n_days, diff.toDays().toString())
        }
        return lastMessageDate
    }
}