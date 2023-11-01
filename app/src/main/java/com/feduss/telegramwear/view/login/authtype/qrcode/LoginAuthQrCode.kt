package com.feduss.telegramwear.view.login.authtype.qrcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.R
import com.feduss.telegramwear.viewmodel.login.authtype.qrcode.LoginAuthQrCodeViewModel
import com.feduss.telegramwear.viewmodel.login.otp.LoginOtpViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginAuthQrCode(
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: LoginAuthQrCodeViewModel = hiltViewModel()
) {

    val qrCode = viewModel.qrCode.collectAsState()
    val state = viewModel.state.collectAsState()

    when(state.value) {

        LoginAuthQrCodeViewModel.State.GoTo2FA -> {
            navController.navigate(Section.Login2FAVerification.baseRoute)
        }

        LoginAuthQrCodeViewModel.State.GoToChatList -> {
            navController.navigate(Section.ChatList.baseRoute)
        }

        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        qrCode.value?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "qrcode")
        } ?: kotlin.run {
            CircularProgressIndicator(

            )
        }
        Text(
            text = stringResource(R.string.login_auth_qr_code_info),
            textAlign = TextAlign.Center
        )
    }
}