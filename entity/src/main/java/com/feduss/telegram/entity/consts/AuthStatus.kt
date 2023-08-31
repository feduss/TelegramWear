package com.feduss.telegram.entity.consts

sealed class AuthStatus {
    data object Initial: AuthStatus()
    data object Unknown: AuthStatus()
    data object Waiting2FA: AuthStatus()
    data object WaitingOTP: AuthStatus()
    data object ClientLoggedIn: AuthStatus()
}
