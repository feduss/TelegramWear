package com.feduss.telegramwear.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.feduss.telegram.entity.consts.TdLibParam
import com.feduss.telegramwear.data.response.LoadChatResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.SetLogVerbosityLevel
import org.drinkless.td.libcore.telegram.TdApi.UpdateChatTitle
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
    fun getChats(): Flow<List<TdApi.Chat>>
}

class ClientRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
): ClientRepository {
    internal lateinit var client: Client
    internal var authStatus = MutableStateFlow(-1)

    var chats = MutableStateFlow(
        mapOf<Long, TdApi.Chat>()
    )

    val mainHandler = Handler(Looper.getMainLooper())

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
        client = Client.create({ tdApiObject ->
            handleUpdate(tdApiObject, appDir)
        },
        null,
        null
        )

    }

    private fun handleUpdate(
        tdApiObject: TdApi.Object?,
        appDir: String
    ) {
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

                updateChats { map ->
                    map[tdApiObject.chat.id] = tdApiObject.chat
                }
                //Log.i("LogTest: ", "new/updated chat with id ${tdApiObject.chat.id}...chats size: ${chats.value.size}")
            }

            is TdApi.UpdateChatPhoto -> {

                updateChats { map ->
                    map[tdApiObject.chatId]?.photo = tdApiObject.photo
                }
            }

            is UpdateChatTitle -> {

                updateChats { map ->
                    map[tdApiObject.chatId]?.title = tdApiObject.title
                }
            }

            is TdApi.UpdateChatLastMessage -> {

                updateChats { map ->
                    map[tdApiObject.chatId]?.lastMessage = tdApiObject.lastMessage
                }
            }

            is TdApi.UpdateChatPosition -> {

                updateChats { map ->
                    val newPosition = tdApiObject.position

                    if (newPosition.order == 0L){
                        map.remove(tdApiObject.chatId)
                    } else {
                        val oldPositions = chats.value[tdApiObject.chatId]?.positions
                        val newSize = (oldPositions?.size ?: 0) + 1
                        val newPositions = Array<TdApi.ChatPosition>(newSize) { newPosition }
                        oldPositions?.iterator()?.withIndex()?.forEach { item ->
                            newPositions[item.index + 1] = item.value
                        }

                        map[tdApiObject.chatId]?.positions = newPositions
                    }
                }
            }

            is TdApi.UpdateChatNotificationSettings -> {

                updateChats { map ->
                    map[tdApiObject.chatId]?.notificationSettings = tdApiObject.notificationSettings
                }



            }
        }
    }

    private fun updateChats(updateHandler: (MutableMap<Long, TdApi.Chat>) -> Unit) {
        synchronized(chats) {
            chats.update { immutableMap ->
                immutableMap.toMutableMap().apply {
                    updateHandler(this)
                }
            }
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
                    chatList,
                    limit,
                    completionDeferred
                )
                //completionDeferred.complete(LoadChatResponse.ChatUpdated)
            } else {
                Log.e("LogTest: ", "retrieveChat error --> $tdApiObject")
            }
        }
    }

    override fun getChats(): Flow<List<TdApi.Chat>> {
        return chats.map {
            it.values.toList()
        }
    }
}