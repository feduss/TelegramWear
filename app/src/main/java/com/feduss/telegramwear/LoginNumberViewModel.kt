package com.feduss.telegramwear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginNumberViewModel : ViewModel() {
    var phoneNumber = ""
    var isConfirmButtonEnabled = MutableLiveData(false)

    fun userHasUpdatedPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber.trim()
        this.isConfirmButtonEnabled.value = phoneNumber.isNotEmpty()
    }
}