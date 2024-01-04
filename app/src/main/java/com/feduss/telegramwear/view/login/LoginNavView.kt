package com.feduss.telegramwear.view.login

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.view.component.PageView
import com.feduss.telegramwear.view.login.authtype.LoginAuthChoice
import com.feduss.telegramwear.view.login.authtype.phonenumber.LoginAuthPhoneNumber
import com.feduss.telegramwear.view.login.authtype.qrcode.LoginAuthQrCode
import com.feduss.telegramwear.view.login.otp.LoginOtpVerification
import com.feduss.telegramwear.view.login.twofa.Login2FAVerification
import com.feduss.telegramwear.view.login.welcome.LoginWelcomePage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.scrollable

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginNavView(
    activity: MainActivityViewController,
    startDestination: String = Section.LoginWelcomePage.baseRoute
) {

    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        modifier = Modifier.background(Color.Black),
        navController = navController,
        startDestination = startDestination
    ) {

        scrollable(route = Section.LoginWelcomePage.baseRoute) {
            PageView {
                LoginWelcomePage(
                    navController = navController,
                    columnState = it
                )
            }
        }

        scrollable(route = Section.LoginAuthChoice.baseRoute) {

            PageView {
                LoginAuthChoice(
                    navController = navController,
                    columnState = it
                )
            }
        }

        scrollable(route = Section.LoginAuthPhoneNumber.baseRoute) {
            PageView {
                LoginAuthPhoneNumber(
                    navController = navController,
                    columnState = it
                )
            }
        }

        scrollable(route = Section.LoginAuthQrCode.baseRoute) {
            PageView {
                LoginAuthQrCode(
                    navController = navController,
                    columnState = it
                )
            }
        }

        scrollable(route = Section.LoginOtpVerification.baseRoute) {
            PageView {
                LoginOtpVerification(
                    navController = navController,
                    columnState = it
                )
            }
        }

        scrollable(route = Section.Login2FAVerification.baseRoute) {
            PageView {
                Login2FAVerification(
                    navController = navController,
                    columnState = it
                )
            }
        }
    }
}