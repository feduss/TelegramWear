package com.feduss.telegramwear.login.phoneNumber

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class LoginPhoneNumberViewModel(application: Application) :
    AndroidViewModel(application) {
    var phoneNumber = ""
    var isConfirmButtonEnabled = MutableLiveData(false)

    fun userHasUpdatedPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber.trim()
        this.isConfirmButtonEnabled.value = phoneNumber.isNotEmpty()
    }
}