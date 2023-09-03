package com.feduss.telegramwear.viewmodel.login.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegramwear.business.ClientInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginOtpViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
): ViewModel() {

    sealed class State {
        data object GoTo2FA: State()
    }

    private val _state = MutableStateFlow<State?>(null)
    var state = _state.asStateFlow()

    private val _isConfirmButtonEnabled = MutableStateFlow(false)
    var isConfirmButtonEnabled = _isConfirmButtonEnabled.asStateFlow()

    private val _isOtpValid = MutableStateFlow<Boolean?>(null)
    var isOtpValid = _isOtpValid.asStateFlow()

    init {
        viewModelScope.launch {
            check2FA()
        }
    }

    fun userHasEditedOtp(otp: String) {
        _isConfirmButtonEnabled.value = otp.isNotBlank()
    }

    suspend fun checkOtp(otp: String) {
        clientInteractor.checkOTP(otp).collectLatest { isSuccess ->
            _isOtpValid.value = isSuccess
        }
    }

    private suspend fun check2FA() {
        clientInteractor.getAuthStatus().collect { authStatus ->
            if (_isOtpValid.value == true) {
                when(authStatus) {
                    AuthStatus.Waiting2FA -> _state.value = State.GoTo2FA
                    else -> {}
                }
            }
        }
    }
}