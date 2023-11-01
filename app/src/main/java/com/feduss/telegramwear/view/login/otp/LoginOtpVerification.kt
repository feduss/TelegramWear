package com.feduss.telegramwear.view.login.otp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors
import com.feduss.telegramwear.view.component.button.TextButton
import com.feduss.telegramwear.view.component.textfield.AutoFocusedCustomTextField
import com.feduss.telegramwear.viewmodel.login.otp.LoginOtpViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginOtpVerification(
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: LoginOtpViewModel = hiltViewModel()
) {

    var isProgressBarVisible by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val isConfirmButtonEnabled = viewModel.isConfirmButtonEnabled.collectAsState()
    val isOtpValid = viewModel.isOtpValid.collectAsState()
    val state = viewModel.state.collectAsState()

    when(state.value) {

        LoginOtpViewModel.State.GoTo2FA -> {
            navController.navigate(Section.Login2FAVerification.baseRoute)
        }

        LoginOtpViewModel.State.GoToChatList -> {
            navController.navigate(Section.ChatList.baseRoute)
        }

        else -> {}
    }

    if (isProgressBarVisible) {

        if (isOtpValid.value == false) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                indicatorColor = Color.Red,
                strokeWidth = 4.dp,
                progress = 1f
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                indicatorColor = AppColors.TelegramBlue.toColor(),
                strokeWidth = 4.dp
            )
        }
    }

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp),
        columnState = columnState
    ){

        item {
            Text(
                text = stringResource(id = R.string.login_otp_title),
                textAlign = TextAlign.Center
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            AutoFocusedCustomTextField(
                backgroundColor = AppColors.TelegramBlue.toColor(),
                value = otp,
                placeholder = "12345",
                isError = isOtpValid.value == false,
                errorText = stringResource(id = R.string.login_otp_wrong_code),
                keyboardType = KeyboardType.Password,
                onValueChange = {
                    otp = it
                    viewModel.userHasEditedOtp(it)
                },
                focusRequester = focusRequester
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            TextButton(
                enabled = isConfirmButtonEnabled.value == true,
                onClick = {
                    isProgressBarVisible = true
                    isProgressBarVisible = true
                    coroutineScope.launch {
                        viewModel.checkOtp(otp)
                    }
                },
                title = stringResource(id = R.string.login_number_confirm_button)
            )
        }
    }
}