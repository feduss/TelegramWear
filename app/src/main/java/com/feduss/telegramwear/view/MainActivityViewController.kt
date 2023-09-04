package com.feduss.telegramwear.view

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import com.feduss.telegram.entity.consts.AuthStatus
import com.feduss.telegramwear.R
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

            if (authStatus.value == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        modifier = Modifier.height(48.dp),
                        painter = painterResource(id = R.drawable.ic_splashscreen_background),
                        contentDescription = stringResource(id = R.string.welcome_title_icon_desc),
                        tint = Color.Unspecified
                    )

                    CircularProgressIndicator()
                }
            } else if (authStatus.value == AuthStatus.ClientLoggedIn) {
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