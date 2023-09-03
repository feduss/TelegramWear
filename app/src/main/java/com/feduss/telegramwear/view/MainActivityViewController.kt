package com.feduss.telegramwear.view

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegramwear.view.chat.ChatNavView
import com.feduss.telegramwear.view.login.LoginNavView
import com.feduss.telegramwear.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivityViewController : ComponentActivity() {

    private lateinit var context: Context
    private lateinit var activityViewController: MainActivityViewController
    private val viewModel: MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        context = this
        activityViewController = this


        setContent {

            val authStatus = viewModel.authStatus.collectAsState()

            if (authStatus.value == AuthStatus.ClientLoggedIn) {
                ChatNavView(
                    activity = activityViewController
                )
            } else {
                LoginNavView(
                    activity = activityViewController
                )
            }
        }
    }
}