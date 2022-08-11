package com.feduss.telegramwear.login.phoneNumber

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginPhoneNumberViewModel : ViewModel() {
    var phoneNumber = ""
    var isConfirmButtonEnabled = MutableLiveData(false)

    fun userHasUpdatedPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber.trim()
        this.isConfirmButtonEnabled.value = phoneNumber.isNotEmpty()
    }
}