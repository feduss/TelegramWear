package com.feduss.telegramwear.viewmodel.login.twofa

import androidx.lifecycle.ViewModel
import com.feduss.telegramwear.business.ClientInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

// TODO: we need to encrypt the password
@HiltViewModel
class Login2FAViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
): ViewModel() {

    private val _isConfirmButtonEnabled = MutableStateFlow(false)
    var isConfirmButtonEnabled = _isConfirmButtonEnabled.asStateFlow()

    private val _isPasswordValidated = MutableStateFlow<Boolean?>(null)
    var isPasswordValidated = _isPasswordValidated.asStateFlow()

    fun userHasEditedPassword(password: String) {
        _isConfirmButtonEnabled.value = password.isNotBlank()
    }

    suspend fun checkPassword(password: String) {
        _isConfirmButtonEnabled.value = false
        clientInteractor.checkPassword(password).collectLatest { isSuccess ->
            _isPasswordValidated.value = isSuccess
            _isConfirmButtonEnabled.value = true
        }
    }
}