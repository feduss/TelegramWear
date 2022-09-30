package com.feduss.telegramwear.login.otp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.feduss.telegramwear.repos.ClientRepository

class LoginOTPViewModel(application: Application) :
    AndroidViewModel(application) {

    val isOTPValid = MutableLiveData<Boolean?>(null)

    fun userHasUpdatedOtp(otp: String) {
        ClientRepository.checkOTP(otp) { isSuccess ->
            isOTPValid.postValue(isSuccess)
        }
    }

    fun is2FAEnabled(): Boolean {
        return ClientRepository.is2FAEnabled()
    }
}