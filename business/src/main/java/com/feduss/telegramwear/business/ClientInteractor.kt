package com.feduss.telegramwear.business

import android.content.Context
import com.feduss.telegram.entity.LoadChatResult
import com.feduss.telegram.entity.LoggedUser
import com.feduss.telegram.entity.QrCodeResult
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegram.entity.model.ChatHistoryItemModel
import com.feduss.telegram.entity.model.ChatListItemModel
import com.feduss.telegramwear.data.ClientRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.drinkless.td.libcore.telegram.TdApi
import javax.inject.Inject

interface ClientInteractor {
    fun logout(): Flow<Boolean>
    fun sendOTP(phoneNumber: String): Flow<Boolean>
    fun checkOTP(otp: String): Flow<Boolean>
    fun retrieveQrCode(): Flow<QrCodeResult>
    fun checkPassword(password: String): Flow<Boolean>
    fun getAuthStatus(): Flow<AuthStatus>
    fun getMe(): Flow<LoggedUser?>
    fun requestChats(limit: Int): Flow<LoadChatResult>
    fun getChatModels(context: Context): Flow<ArrayList<ChatListItemModel>>
}

class ClientInteractorImpl @Inject constructor(
    private val clientRepository: ClientRepository
): ClientInteractor {

    override fun logout(): Flow<Boolean> {
        return clientRepository.logout()
    }

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

    override fun getMe(): Flow<LoggedUser?> {
        return clientRepository.getMe()
    }

    // TODO: temp impl
    override fun requestChats(limit: Int): Flow<LoadChatResult> {

        return clientRepository.requestChats(limit)
    }

    override fun getChatModels(context: Context): Flow<ArrayList<ChatListItemModel>> {
        return clientRepository.getChatModels()
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