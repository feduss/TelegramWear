package com.feduss.telegramwear.view.login

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.view.login.authtype.LoginAuthChoice
import com.feduss.telegramwear.view.login.authtype.phonenumber.LoginAuthPhoneNumber
import com.feduss.telegramwear.view.login.authtype.qrcode.LoginAuthQrCode
import com.feduss.telegramwear.view.login.otp.LoginOtpVerification
import com.feduss.telegramwear.view.login.twofa.Login2FAVerification
import com.feduss.telegramwear.view.login.welcome.LoginWelcomePage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import com.google.android.horologist.compose.navscaffold.scrollable
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginNavView(
    activity: MainActivityViewController,
    startDestination: String = Section.LoginWelcomePage.baseRoute
) {

    val navController = rememberSwipeDismissableNavController()

    val timeSource = TimeTextDefaults.timeSource(
        DateFormat.getBestDateTimePattern(Locale.getDefault(), "HH:mm")
    )

    WearNavScaffold(
        modifier = Modifier.background(Color.Black),
        timeText = {
            TimeText(
                timeSource = timeSource,
            )
        },
        navController = navController,
        startDestination = startDestination
    ) {

        scrollable(route = Section.LoginWelcomePage.baseRoute) {
            LoginWelcomePage(
                navController = navController,
                columnState = it.columnState
            )
        }

        scrollable(route = Section.LoginAuthChoice.baseRoute) {
            LoginAuthChoice(
                navController = navController,
                columnState = it.columnState
            )
        }

        scrollable(route = Section.LoginAuthPhoneNumber.baseRoute) {
            LoginAuthPhoneNumber(
                navController = navController,
                columnState = it.columnState
            )
        }

        scrollable(route = Section.LoginAuthQrCode.baseRoute) {
            LoginAuthQrCode(
                navController = navController,
                columnState = it.columnState
            )
        }

        scrollable(route = Section.LoginOtpVerification.baseRoute) {
            LoginOtpVerification(
                navController = navController,
                columnState = it.columnState
            )
        }

        scrollable(route = Section.Login2FAVerification.baseRoute) {
            Login2FAVerification(
                navController = navController,
                columnState = it.columnState
            )
        }
    }
}