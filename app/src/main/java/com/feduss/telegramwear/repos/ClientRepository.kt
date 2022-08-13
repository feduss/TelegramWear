package com.feduss.telegramwear.repos

import android.util.Log
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

object ClientRepository {
    internal lateinit var client: Client

    init {
        println("You're invoking a private constructor")
    }

    fun setupHandler(appDir: String) {
        client = Client.create({ tdApiObject ->
            val authState = (tdApiObject as TdApi.UpdateAuthorizationState).authorizationState
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
        parameters.databaseDirectory = appDir + "/TelegramWear"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = true
        parameters.apiId = 94575  //TODO: to update
        parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2" //TODO: to update
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

    fun fetchQRCodeLink(completion: (String) -> Unit) {
        client.send(TdApi.GetAuthorizationState()) { tdApiObject ->
            //Pending/prev qr code request
            if (tdApiObject.constructor == TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR) {
                getQrCodeResponse(tdApiObject, completion)
            } else {
                requestQrCode(completion)
            }
        }
    }

    private fun requestQrCode(completion: (String) -> Unit) {
        client.send(TdApi.RequestQrCodeAuthentication()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                getQrCodeResponse(tdApiObject, completion)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("Error:", "Can't generate qr code")
            }

        }
    }

    private fun getQrCodeResponse(
        tdApiObject: TdApi.Object?,
        completion: (String) -> Unit
    ) {
        val qrCodeAuthResult = tdApiObject as TdApi.AuthorizationStateWaitOtherDeviceConfirmation
        Log.i("Ok", "Qr code generated")
        println("QrCode link -> " + qrCodeAuthResult.link)
        completion(qrCodeAuthResult.link)
    }

}