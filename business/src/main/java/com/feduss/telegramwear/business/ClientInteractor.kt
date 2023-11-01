package com.feduss.telegramwear.business

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegram.entity.model.ChatItemModel
import com.feduss.telegramwear.business.result.LoadChatResult
import com.feduss.telegramwear.business.result.QrCodeResult
import com.feduss.telegramwear.data.ClientRepository
import com.feduss.telegram.entity.QrCodeResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.InputMessageText
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.Instant.now
import javax.inject.Inject

interface ClientInteractor {
    fun sendOTP(phoneNumber: String): Flow<Boolean>
    fun checkOTP(otp: String): Flow<Boolean>
    fun retrieveQrCode(): Flow<QrCodeResult>
    fun checkPassword(password: String): Flow<Boolean>
    fun getAuthStatus(): Flow<AuthStatus>
    fun retrieveChats(limit: Int): Flow<LoadChatResult>
    fun getChats(context: Context): Flow<ArrayList<ChatItemModel>>
}

class ClientInteractorImpl @Inject constructor(
    private val clientRepository: ClientRepository
): ClientInteractor {

    override fun sendOTP(phoneNumber: String): Flow<Boolean> {
        return clientRepository.sendOTP(phoneNumber)
    }

    override fun checkOTP(otp: String): Flow<Boolean> {
        return clientRepository.checkOTP(otp)
    }

    override fun retrieveQrCode() = flow {
        val completableDeferred = CompletableDeferred<QrCodeResult>()
        clientRepository.fetchQRCodeLink().collectLatest { prevQrCode ->
            if (prevQrCode != QrCodeResult.Error) {
                completableDeferred.complete(prevQrCode)
            } else {
                clientRepository.requestQrCode().collectLatest { newQrCode ->
                    completableDeferred.complete(newQrCode)
                }
            }
        }

        emit(completableDeferred.await())
    }

    override fun checkPassword(password: String): Flow<Boolean> {
        return clientRepository.checkPassword(password)
    }

    override fun getAuthStatus(): Flow<AuthStatus> {

        return clientRepository.getAuthStatus().map { rawStatus ->
            when (rawStatus) {
                TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> AuthStatus.Waiting2FA
                TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> AuthStatus.WaitingOTP
                TdApi.AuthorizationStateReady.CONSTRUCTOR -> AuthStatus.ClientLoggedIn
                else -> AuthStatus.Unknown
            }
        }

    }

    // TODO: temp impl
    override fun retrieveChats(limit: Int): Flow<LoadChatResult> {

        return clientRepository.retrieveChats(limit).map { result ->
            when(result) {
                is LoadChatResponse.ChatUpdated -> {
                    LoadChatResult.ChatUpdated
                }

                is LoadChatResponse.NoMoreChat -> {
                    LoadChatResult.NoMoreChat
                }

                else -> {
                    LoadChatResult.LoadingError
                }
            }
        }
    }

    override fun getChats(context: Context): Flow<ArrayList<ChatItemModel>> {

        return clientRepository.getChats().map { rawChats ->
            val orders = rawChats.chats.associate {
                    chat -> chat.id to chat.positions.firstOrNull() {
                    position -> position.order > 0
                }?.order
            }

            val sortedRawChats = rawChats.chats.sortedByDescending { orders[it.id] }

            val resultChats = sortedRawChats.map { chat ->

                val personName = chat.title ?: context.getString(R.string.chat_title_deleted_account)

                val image = getProfilePhoto(chat, personName)

                var (lastMessageImage: Bitmap?, lastMessage) = getLastMessage(chat, context)

                val lastMessageDate: String = getLastMessageDate(
                    chat,
                    context
                )


                val isPinned = chat.positions.count { it.isPinned } != 0
                val isMuted = chat.notificationSettings.muteFor != 0
                val unreadMessagesCount = chat.unreadCount

                //TODO: to remove, only for app demonstration purpose
                //val replacementText: String = lastMessage.map { '*' }.joinToString("")
                //lastMessage = lastMessage.trim().replaceRange(0, lastMessage.length, replacementText)
                //

                val chatItemModel = ChatItemModel(
                    image = image,
                    personName = personName.trim(),
                    lastMessageImage = lastMessageImage,
                    lastMessage = lastMessage,
                    lastMessageDate = lastMessageDate,
                    unreadMessagesCount = unreadMessagesCount,
                    isPinned = isPinned,
                    isMuted = isMuted,
                    isOnline = rawChats.usersStatus[chat.id] == true
                )

                chatItemModel
            }

            ArrayList(resultChats)
        }
        .flowOn(Dispatchers.IO)
    }

    private fun getProfilePhoto(chat: TdApi.Chat, personName: String): Bitmap {
        val photoData = chat.photo?.small?.local?.path
        val image: Bitmap = try {

            if (File(photoData ?: "").exists()) {
                BitmapFactory.decodeFile(photoData)
            } else {
                getDefaultImageWithText(personName, chat.id)
            }
        } catch(e: Exception) {
            getDefaultImageWithText(personName, chat.id)
        }

        return image
    }

    private fun getLastMessage(chat: TdApi.Chat,context: Context): Pair<Bitmap?, String> {
        var lastMessageImage: Bitmap? = null
        val lastMessage: String

        val draftMessage = chat.draftMessage
        if (draftMessage != null) {
            val inputMessageText = draftMessage.inputMessageText as InputMessageText
            lastMessage = context.getString(R.string.last_message_draft, inputMessageText.text.text)
        } else {
            when (val chatContent = chat.lastMessage?.content) {
                is TdApi.MessageText -> {
                    lastMessage = chatContent.text.text
                }

                is TdApi.MessageDocument -> {
                    lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_file)
                    lastMessage = chatContent.document.fileName
                }

                is TdApi.MessagePhoto -> {
                    lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_photo)
                    lastMessage = "Foto"
                }

                is TdApi.MessageVideo -> {
                    lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_video)
                    lastMessage = "Video"
                }

                is TdApi.MessageVoiceNote -> {
                    lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_music)
                    lastMessage = "Audio"
                }

                is TdApi.MessageAnimation -> {
                    lastMessageImage = getBitmapFromVectorDrawable(context, R.drawable.ic_gif)
                    lastMessage = "GIF"
                }

                is TdApi.MessageSticker -> {
                    val stickerEmoji = chatContent.sticker.emoji
                    lastMessage = "${stickerEmoji} Sticker"
                }

                is TdApi.MessageAnimatedEmoji -> {
                    val stickerEmoji = chatContent.emoji
                    lastMessage = "${stickerEmoji} Sticker"
                }

                else -> {
                    lastMessage = context.getString(R.string.last_message_unsupported_message)
                }
            }
        }
        return Pair(lastMessageImage, lastMessage)
    }

    private fun getLastMessageDate(chat: TdApi.Chat, context: Context): String {
        val rawLastMessageDate = Instant.ofEpochSecond(
            chat.lastMessage?.date?.toLong() ?: 0L
        )

        val diff: Duration = Duration.between(
            rawLastMessageDate,
            now()
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
        textPaint.textAlign = Align.CENTER
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

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}