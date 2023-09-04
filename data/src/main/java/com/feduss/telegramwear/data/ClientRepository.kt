package com.feduss.telegramwear.data

import android.content.Context
import android.util.Log
import com.feduss.telegram.entity.consts.TdLibParam
import com.feduss.telegramwear.data.response.LoadChatResponse
import com.feduss.telegramwear.data.response.RawChat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.SetLogVerbosityLevel
import org.drinkless.td.libcore.telegram.TdApi.UpdateChatTitle
import org.drinkless.td.libcore.telegram.TdApi.UserStatusOnline
import java.io.File
import javax.inject.Inject


interface ClientRepository {
    fun sendOTP(phoneNumber: String): Flow<Boolean>
    fun checkOTP(otp: String): Flow<Boolean>
    fun fetchQRCodeLink(): Flow<String?>
    fun requestQrCode(): Flow<String?>
    fun checkPassword(password: String): Flow<Boolean>
    fun getAuthStatus(): Flow<Int>
    fun retrieveChats(limit: Int): Flow<LoadChatResponse>
    fun getChats(): Flow<RawChat>
}

class ClientRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
): ClientRepository {
    private lateinit var client: Client
    private var authStatus = MutableStateFlow(-1)

    @OptIn(DelicateCoroutinesApi::class)
    private val threadContext = newSingleThreadContext("threadContext")

    private var chats = mapOf<Long, TdApi.Chat>()
    private var chatsFlow = MutableSharedFlow<Map<Long, TdApi.Chat>>(
        replay = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var usersStatus = mapOf<Long, Boolean?>()

    private var usersStatusFlow = MutableSharedFlow<Map<Long, Boolean?>>(
    )

    init {
        val appDir = context.getExternalFilesDir(null).toString()
        val dir = File(appDir + "TelegramWear/tdlib")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        setupHandler(appDir)
    }

    // Init fun

    private fun setupHandler(appDir: String) {
        client = Client.create(
            //updateHandler
            { tdApiObject ->
                handleUpdate(tdApiObject, appDir)
            },
            //updateExceptionHandler
            {
                //Log.e("LogTest: ", "updateExceptionHandler: ${it.localizedMessage}")
            },
            //defaultExceptionHandler
            {
                //Log.e("LogTest: ", "defaultExceptionHandler: ${it.localizedMessage}")
            }
        )

    }

    private fun handleUpdate(
        tdApiObject: TdApi.Object?,
        appDir: String
    ) {
        //Log.i("Status update", " --> $tdApiObject")
        when (tdApiObject) {
            is TdApi.UpdateAuthorizationState -> {
                val authState = tdApiObject.authorizationState
                authStatus.value = authState.constructor
                if (authState.constructor == TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR) {
                    Log.i("LogTest: ", "Client created")
                    setTdLibParams(appDir)
                }
            }

            is TdApi.UpdateNewChat -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chat.id] = tdApiObject.chat
                    newChats
                }
            }

            is TdApi.UpdateChatPhoto -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.photo = tdApiObject.photo
                    newChats
                }
            }

            is UpdateChatTitle -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.title = tdApiObject.title
                    newChats
                }
            }

            is TdApi.UpdateChatLastMessage -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.lastMessage = tdApiObject.lastMessage
                    newChats
                }
            }

            is TdApi.UpdateChatPosition -> {

                updateChats { newChats ->
                    val newPosition = tdApiObject.position

                    if (newPosition.order == 0L){
                        newChats.remove(tdApiObject.chatId)
                    } else {
                        val oldPositions = chats[tdApiObject.chatId]?.positions
                        val newSize = (oldPositions?.size ?: 0) + 1
                        val newPositions = Array<TdApi.ChatPosition>(newSize) { newPosition }
                        oldPositions?.iterator()?.withIndex()?.forEach { item ->
                            newPositions[item.index + 1] = item.value
                        }

                        newChats[tdApiObject.chatId]?.positions = newPositions
                    }

                    newChats
                }
            }

            is TdApi.UpdateChatNotificationSettings -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.notificationSettings = tdApiObject.notificationSettings
                    newChats
                }
            }

            is TdApi.UpdateNewMessage -> {

                updateChats { newChats ->
                    newChats[tdApiObject.message.chatId]?.lastMessage = tdApiObject.message
                    newChats
                }
            }

            is TdApi.UpdateMessageEdited -> { /*Handle in other state */ }

            is TdApi.UpdateMessageContent -> {
                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.lastMessage?.content = tdApiObject.newContent
                    newChats
                }
            }

            is TdApi.UpdateDeleteMessages -> { /*Handle in other state */ }

            is TdApi.UpdateUserStatus -> {

                updateUsersStatus { newUsersStatus ->
                    newUsersStatus[tdApiObject.userId] = tdApiObject.status is UserStatusOnline
                    newUsersStatus
                }
            }

            is TdApi.UpdateChatDraftMessage -> {

                updateChats { newChats ->

                    newChats[tdApiObject.chatId]?.draftMessage = tdApiObject.draftMessage
                    newChats
                }
            }

            else -> {
                //Log.i("OtherStatus", " --> $tdApiObject")
            }
        }
    }

    private fun updateChats(updateHandler: (MutableMap<Long, TdApi.Chat>) -> MutableMap<Long, TdApi.Chat>) {

        val updatedChats = updateHandler(chats.toMutableMap())
        chats = updatedChats
        runBlocking(threadContext) {
            chatsFlow.emit(updatedChats)

        }

    }

    private fun updateUsersStatus(updateHandler: (MutableMap<Long, Boolean?>) -> MutableMap<Long, Boolean?>) {
        val updatedUsersStatus = updateHandler(usersStatus.toMutableMap())
        usersStatus = updatedUsersStatus

        runBlocking(threadContext) {
            usersStatusFlow.emit(updatedUsersStatus)
        }
    }

    private fun setTdLibParams(appDir: String) {
        val parameters = TdApi.TdlibParameters()
        parameters.useTestDc = false
        parameters.databaseDirectory = "$appDir/TelegramWear"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = true
        parameters.apiId = Integer.parseInt(TdLibParam.ApiId.value)
        parameters.apiHash = TdLibParam.ApiHash.value
        parameters.systemLanguageCode = "it"
        parameters.deviceModel = "Mobile"
        parameters.applicationVersion = "0.1"
        parameters.enableStorageOptimizer = true
        client.send(TdApi.SetTdlibParameters(parameters)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "tdlib params set")
                checkDBEncryptionKey()
            } else {
                Log.e("LogTest: ", "Can't set tdlib params")
            }
        }

        client.send(SetLogVerbosityLevel(0)) {}
    }

    private fun checkDBEncryptionKey() {
        client.send(TdApi.CheckDatabaseEncryptionKey()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "Database encryption key checked")
            } else {
                Log.e("LogTest: ", "Can't check database encryption key")
            }
        }
    }

    // OTP

    override fun sendOTP(phoneNumber: String): Flow<Boolean> = flow {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "Otp sent")
                completionDeferred.complete(true)
            } else {
                Log.e("LogTest: ", "Otp error")
                completionDeferred.complete(false)
            }
        }

        emit(completionDeferred.await())
    }

    override fun checkOTP(otp: String): Flow<Boolean> = flow {
        val completionDeferred = CompletableDeferred<Boolean>()

        client.send(TdApi.CheckAuthenticationCode(otp)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("LogTest: ", "Wrong otp")
                completionDeferred.complete(false)
            }
        }

        emit(completionDeferred.await())
    }

    // Qr code

    override fun fetchQRCodeLink() = flow {
        val completionDeferred = CompletableDeferred<String?>()
        client.send(TdApi.GetAuthorizationState()) { tdApiObject ->
            //Pending/prev qr code request
            if (tdApiObject.constructor == TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR) {
                completionDeferred.complete(getQrCodeResponse(tdApiObject))
            } else {
                completionDeferred.complete(null)
            }
        }
        emit(completionDeferred.await())
    }

    override fun requestQrCode() = flow {
        val completionDeferred = CompletableDeferred<String?>()
        client.send(TdApi.RequestQrCodeAuthentication()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(getQrCodeResponse(tdApiObject))
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("LogTest: ", "Can't generate qr code")
                completionDeferred.complete(null)
            }

        }
        emit(completionDeferred.await())
    }

    private fun getQrCodeResponse(tdApiObject: TdApi.Object?): String? {
        return if(tdApiObject is TdApi.AuthorizationStateWaitOtherDeviceConfirmation) {
            Log.i("LogTest: ", "Qr code generated")
            tdApiObject.link
        } else {
            null
        }
    }

    //2FA

    override fun checkPassword(password: String) = flow<Boolean> {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.CheckAuthenticationPassword(password)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("LogTest: ", "Wrong password")
                completionDeferred.complete(false)
            }
        }
        emit(completionDeferred.await())
    }

    //

    override fun getAuthStatus(): Flow<Int> {
        return authStatus
    }

    // Chat list

    override fun retrieveChats(limit: Int) = flow {
        val completionDeffered = CompletableDeferred<LoadChatResponse>()

        downloadChats(
            TdApi.ChatListMain(),
            limit,
            completionDeffered
        )

        emit(completionDeffered.await())
    }

    private fun downloadChats(
        chatList: TdApi.ChatList,
        limit: Int,
        completionDeferred: CompletableDeferred<LoadChatResponse>
    ) {
        client.send(TdApi.LoadChats(chatList, limit)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                if (tdApiObject is TdApi.Error && tdApiObject.code == 404) {
                    Log.e("LogTest: ", "retrieveChat error 404 --> ${tdApiObject.message}")
                    completionDeferred.complete(LoadChatResponse.NoMoreChat)
                } else {
                    Log.e("LogTest: ", "retrieveChat error other --> $tdApiObject")
                    completionDeferred.complete(LoadChatResponse.LoadingError)
                }
            } else if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "retrieveChat downloading chats")
                downloadChats(
                    chatList = chatList,
                    limit = limit,
                    completionDeferred = completionDeferred
                )
                //completionDeferred.complete(LoadChatResponse.ChatUpdated)
            } else {
                Log.e("LogTest: ", "retrieveChat error --> $tdApiObject")
            }
        }
    }

    override fun getChats(): Flow<RawChat> {

        return chatsFlow.combine(usersStatusFlow) { chats, usersStatus ->
            //Log.i("LogTest: ", "Triggered with chat size ${chats.size}")
            val transformedChats: List<TdApi.Chat> = chats.map {
                it.value
            }

            RawChat(
                transformedChats,
                usersStatus
            )
        }
        .flowOn(threadContext)
    }
}