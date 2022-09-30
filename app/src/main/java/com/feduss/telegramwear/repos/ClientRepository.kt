package com.feduss.telegramwear.repos

import android.util.Log
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

object ClientRepository {
    internal lateinit var client: Client
    internal var status: Int = -1

    init {
        println("You're invoking a private constructor")
    }

    fun setupHandler(appDir: String) {
        client = Client.create({ tdApiObject ->
            val authState = (tdApiObject as TdApi.UpdateAuthorizationState).authorizationState
            this.status = authState.constructor
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
            this.status = tdApiObject.constructor
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
            this.status = tdApiObject.constructor
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("Ok", "Database encryption key checked")
            } else {
                Log.e("Error:", "Can't check database encryption key")
            }
        }
    }

    internal fun fetchQRCodeLink(completion: (String) -> Unit) {
        client.send(TdApi.GetAuthorizationState()) { tdApiObject ->
            this.status = tdApiObject.constructor
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
            this.status = tdApiObject.constructor
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
        if (tdApiObject != null) {
            this.status = tdApiObject.constructor
        }
        val qrCodeAuthResult = tdApiObject as TdApi.AuthorizationStateWaitOtherDeviceConfirmation
        Log.i("Ok", "Qr code generated")
        println("QrCode link -> " + qrCodeAuthResult.link)
        completion(qrCodeAuthResult.link)
    }

    internal fun sendOTP(phoneNumber: String, completion: (Boolean) -> Unit) {
        client.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { tdApiObject ->
            this.status = tdApiObject.constructor
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("Ok", "Otp sent")
                completion(true)
            } else {
                Log.i("Error", "Otp error")
                completion(false)
            }


        }
    }

    internal fun checkOTP(otp: String, completion: (Boolean) -> Unit) {
        client.send(TdApi.CheckAuthenticationCode(otp)) { tdApiObject ->
            this.status = tdApiObject.constructor
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completion(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("Error:", "Wrong otp")
                completion(false)
            }

        }
    }

    internal fun checkPassword(password: String, completion: (Boolean) -> Unit) {
        client.send(TdApi.CheckAuthenticationPassword(password)) { tdApiObject ->
            this.status = tdApiObject.constructor
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completion(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("Error:", "Wrong password")
                completion(false)
            }
        }
    }

    internal fun is2FAEnabled(): Boolean {
        return this.status == TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR
    }

    @JvmName("getStatus")
    internal fun getStatus(): Int {
        return this.status
    }

}