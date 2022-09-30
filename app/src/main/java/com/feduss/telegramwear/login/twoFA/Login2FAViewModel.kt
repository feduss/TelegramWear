package com.feduss.telegramwear.login.twoFA

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.feduss.telegramwear.repos.ClientRepository

class Login2FAViewModel(application: Application) :
    AndroidViewModel(application) {
    var password = ""
    var isConfirmButtonEnabled = MutableLiveData(false)

    fun userHasUpdatedPassword(password: String) {
        this.password = password.trim()
        this.isConfirmButtonEnabled.value = password.isNotBlank()
    }

    fun checkPassword(completion: (Boolean) -> Unit) {
        ClientRepository.checkPassword(this.password) { isSuccess ->
            completion(isSuccess)
        }
    }
}