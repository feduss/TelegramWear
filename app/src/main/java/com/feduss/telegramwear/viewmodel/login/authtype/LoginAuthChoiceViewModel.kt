package com.feduss.telegramwear.viewmodel.login.authtype

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegramwear.business.ClientInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginAuthChoiceViewModel @Inject constructor(
    private val clientInteractor: ClientInteractor
): ViewModel(), DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        viewModelScope.launch {
            //logout()
        }
    }

    //TODO: implement logout when the user came back to this page...
    //TODO: if you launch logout directly, it may finish during login flow, causing some problems...
    private suspend fun logout() {

        clientInteractor.getAuthStatus().collectLatest {
            if (it != AuthStatus.Unknown) {
                clientInteractor.logout().collectLatest {

                }
            }
        }
    }
}