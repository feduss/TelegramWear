package com.feduss.telegramwear.business

import android.R.attr.text
import android.R.attr.x
import android.R.attr.y
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegram.entity.model.ChatItemModel
import com.feduss.telegramwear.business.result.LoadChatResult
import com.feduss.telegramwear.business.result.QrCodeResult
import com.feduss.telegramwear.data.ClientRepository
import com.feduss.telegramwear.data.response.LoadChatResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import org.drinkless.td.libcore.telegram.TdApi
import javax.inject.Inject


interface ClientInteractor {
    fun sendOTP(phoneNumber: String): Flow<Boolean>
    fun checkOTP(otp: String): Flow<Boolean>
    suspend fun retrieveQrCode(): Flow<QrCodeResult>
    fun checkPassword(password: String): Flow<Boolean>
    fun getStatus(): LiveData<AuthStatus>
    fun getMainChat(context: Context, limit: Int): Flow<LoadChatResult>
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

    // TODO: improve this fun
    override suspend fun retrieveQrCode() = flow {
        val completionDeferred = CompletableDeferred<QrCodeResult>()
        clientRepository.fetchQRCodeLink().collectLatest { prevValidQrCode ->
            if(prevValidQrCode == null) {
                clientRepository.requestQrCode().collectLatest { newQrCode ->
                    if (newQrCode == null) {
                        completionDeferred.complete(QrCodeResult.Error)
                    } else {
                        completionDeferred.complete(QrCodeResult.ValidQrCode(newQrCode))
                    }
                }
            } else {
                completionDeferred.complete(QrCodeResult.ValidQrCode(prevValidQrCode))
            }
        }

        emit(completionDeferred.await())
    }

    override fun checkPassword(password: String): Flow<Boolean> {
        return clientRepository.checkPassword(password)
    }

    override fun getStatus(): LiveData<AuthStatus> {

        return Transformations.map(clientRepository.getStatus()) { rawStatus ->
            when (rawStatus) {
                TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> AuthStatus.Waiting2FA
                TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> AuthStatus.WaitingOTP
                TdApi.AuthorizationStateReady.CONSTRUCTOR -> AuthStatus.ClientLoggedIn
                else -> AuthStatus.Unknown
            }
        }

    }

    //TODO: to test
    override fun getMainChat(context: Context, limit: Int) = flow {
        val completionDeferred = CompletableDeferred<LoadChatResult>()
        clientRepository.getMainChat(limit).collect() { result ->
            when(result) {
                is LoadChatResponse.ChatList -> {

                    val resultChats = result.chats.mapNotNull {chat ->

                        val personName = chat.title ?: "No name"

                        //TODO: wrong photo
                        val photoData = chat.photo?.small?.local?.path
                        var image = BitmapFactory.decodeFile(photoData)

                        if(image == null) {
                            image = getDefaultImageWithText(context, personName)
                        }

                        var lastMessageImage: Bitmap? = null
                        var lastMessage = ""

                        val chatContent = chat.lastMessage?.content

                        when(chatContent) {
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

                            else -> {
                                lastMessage = "Messaggio non supportato"
                            }

                        }

                        val chatItemModel = ChatItemModel(
                            image = image,
                            personName = personName,
                            lastMessageImage = lastMessageImage,
                            lastMessage = lastMessage,
                        )

                        chatItemModel
                    }

                    completionDeferred.complete(LoadChatResult.ChatList(resultChats))
                }

                is LoadChatResponse.NoMoreChat -> {
                    completionDeferred.complete(LoadChatResult.NoMoreChat)
                }

                is LoadChatResponse.LoadingError -> {
                    completionDeferred.complete(LoadChatResult.LoadingError)
                }
            }
        }

        emit(completionDeferred.await())
    }

    private fun getDefaultImageWithText(context: Context, personName: String): Bitmap {

        val colors = listOf(
            Color.Red,
            Color.Blue,
            Color.Green,
            Color.Yellow,
            Color.Cyan,
            Color.LightGray
        )

        val image = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val randomColor = colors.random()
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
            "${splittedStrings[0].first().uppercase()}${splittedStrings[1].first().uppercase()}"
        } else {
            splittedStrings[0].first().toString().uppercase()
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