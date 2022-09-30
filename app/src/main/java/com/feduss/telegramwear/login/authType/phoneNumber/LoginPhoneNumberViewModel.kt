package com.feduss.telegramwear.login.authType.phoneNumber

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.feduss.telegramwear.repos.ClientRepository

class LoginPhoneNumberViewModel(application: Application) :
    AndroidViewModel(application) {
    var phoneNumber = ""
    var isConfirmButtonEnabled = MutableLiveData(false)
    var isOTPSent = MutableLiveData<Boolean?>(null)

    fun userHasUpdatedPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber.trim()
        this.isConfirmButtonEnabled.value = phoneNumber.isNotBlank()
    }

    fun sendOTP() {
        ClientRepository.sendOTP(this.phoneNumber) { isSuccess ->
            isOTPSent.postValue(isSuccess)
        }
    }
}