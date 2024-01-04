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
    fun getChatHistory(context: Context, chatId: Long, lastMessageId: Long, fetchLimit: Int): Flow<ArrayList<ChatHistoryItemModel>>
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

    override fun getChatHistory(
        context: Context, chatId: Long, lastMessageId: Long, fetchLimit: Int
    ): Flow<ArrayList<ChatHistoryItemModel>> {
        return clientRepository.getChatHistory(chatId, lastMessageId, fetchLimit)
    }
}