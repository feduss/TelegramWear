package com.feduss.telegramwear.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegramwear.business.ClientInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val clientInteractor: ClientInteractor
): ViewModel() {

    private val _authStatus = MutableStateFlow<AuthStatus>(AuthStatus.Initial)
    var authStatus = _authStatus.asStateFlow()

    init {
        viewModelScope.launch {
            getAuthStatus()
        }
    }

    private suspend fun getAuthStatus() {

        clientInteractor.getStatus().asFlow().collect() { authStatus ->
            _authStatus.value = authStatus
            Log.e("test123: ", "authStatus value: ${_authStatus.value}")
        }
    }
}