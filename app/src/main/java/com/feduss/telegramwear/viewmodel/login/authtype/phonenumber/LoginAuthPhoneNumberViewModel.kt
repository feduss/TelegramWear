package com.feduss.telegramwear.viewmodel.login.authtype.phonenumber

import androidx.lifecycle.ViewModel
import com.feduss.telegramwear.business.ClientInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class LoginAuthPhoneNumberViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
): ViewModel() {

    //TODO: adapt to compose

    private var phoneNumber = ""
    private val _isConfirmButtonEnabled = MutableStateFlow(false)
    var isConfirmButtonEnabled = _isConfirmButtonEnabled.asStateFlow()

    private val _isOtpSent = MutableStateFlow<Boolean?>(null)
    var isOtpSent = _isOtpSent.asStateFlow()

    fun userHasUpdatedPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber.trim()
        _isConfirmButtonEnabled.value = phoneNumber.isNotBlank()
    }

    suspend fun sendOTP() {
        clientInteractor.sendOTP(this.phoneNumber).collectLatest { isOtpSent ->
            _isOtpSent.value = isOtpSent
        }
    }
}