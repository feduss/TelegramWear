package com.feduss.telegramwear.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.feduss.telegram.entity.consts.TdLibParam
import com.feduss.telegramwear.data.response.LoadChatResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File
import javax.inject.Inject


interface ClientRepository {
    fun sendOTP(phoneNumber: String): Flow<Boolean>
    fun checkOTP(otp: String): Flow<Boolean>
    fun fetchQRCodeLink(): Flow<String?>
    fun requestQrCode(): Flow<String?>
    fun checkPassword(password: String): Flow<Boolean>
    fun getStatus(): LiveData<Int>
    fun getMainChat(limit: Int): Flow<LoadChatResponse>
}

class ClientRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
): ClientRepository {
    internal lateinit var client: Client
    internal var status: MutableLiveData<Int> = MutableLiveData(-1)

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
            val authState = (tdApiObject as TdApi.UpdateAuthorizationState).authorizationState
            status.postValue(authState.constructor)
            if (authState.constructor == TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR) {
                Log.i("Ok", "Client created")
                setTdLibParams(appDir)
            }
            //else {
            //    Log.e("Error", "Can't create client")
            //}
        }, null, null)

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
                Log.i("Ok", "tdlib params set")
                checkDBEncryptionKey()
            } else {
                Log.e("Error", "Can't set tdlib params")
            }
        }
    }

    private fun checkDBEncryptionKey() {
        client.send(TdApi.CheckDatabaseEncryptionKey()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("Ok", "Database encryption key checked")
            } else {
                Log.e("Error:", "Can't check database encryption key")
            }
        }
    }

    // OTP

    override fun sendOTP(phoneNumber: String): Flow<Boolean> = flow {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("Ok", "Otp sent")
                completionDeferred.complete(true)
            } else {
                Log.i("Error", "Otp error")
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
                Log.e("Error:", "Wrong otp")
                completionDeferred.complete(false)
            }
        }

        emit(completionDeferred.await())
    }

    // Qr code

    //OK tested
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

    //OK tested
    override fun requestQrCode() = flow {
        val completionDeferred = CompletableDeferred<String?>()
        client.send(TdApi.RequestQrCodeAuthentication()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(getQrCodeResponse(tdApiObject))
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("Error:", "Can't generate qr code")
                completionDeferred.complete(null)
            }

        }
        emit(completionDeferred.await())
    }

    //OK tested
    private fun getQrCodeResponse(tdApiObject: TdApi.Object?): String? {
        return if(tdApiObject is TdApi.AuthorizationStateWaitOtherDeviceConfirmation) {
            Log.i("Ok", "Qr code generated")
            println("QrCode link -> " + tdApiObject.link)
            tdApiObject.link
        } else {
            null
        }
    }

    override fun checkPassword(password: String) = flow<Boolean> {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.CheckAuthenticationPassword(password)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("Error:", "Wrong password")
                completionDeferred.complete(false)
            }
        }
        emit(completionDeferred.await())
    }

    //OK tested
    override fun getStatus(): LiveData<Int> {
        return status
    }

    //TODO: to complete
    override fun getMainChat(limit: Int) = flow {
        val completionDeferred = CompletableDeferred<LoadChatResponse>()

        client.send(TdApi.GetChats(TdApi.ChatListMain(), limit)) { tdApiObject ->
            if(tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                if (tdApiObject is TdApi.Error && tdApiObject.code == 404) {
                    completionDeferred.complete(LoadChatResponse.NoMoreChat)
                } else {
                    completionDeferred.complete(LoadChatResponse.LoadingError)
                }
            }
            else if (tdApiObject is TdApi.Chats) {
                var chats = ArrayList<TdApi.Chat>()
                tdApiObject.chatIds.forEach { chatId ->
                    //Download chat by id
                    client.send(TdApi.GetChat(chatId)) { chatObject ->
                        if (chatObject is TdApi.Chat) {

                            //Download profile photo of user by its id
                            chatObject.photo?.small?.id?.let {
                                client.send(
                                    TdApi.DownloadFile(
                                        it,
                                        32,
                                        0,
                                        0,
                                        false
                                    )
                                ) { fileObject ->

                                    if (fileObject is TdApi.File) {
                                        ///Add photo in chat object
                                        TdApi.UpdateFile(fileObject)

                                        //
                                        chats.add(chatObject)
                                        completionDeferred.complete(LoadChatResponse.ChatList(chats))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        emit(completionDeferred.await())
    }
}