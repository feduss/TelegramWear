package com.feduss.telegramwear.view.login.authtype.phonenumber

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.feduss.telegramwear.viewmodel.login.authtype.phonenumber.LoginAuthPhoneNumberViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginAuthPhoneNumber(
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: LoginAuthPhoneNumberViewModel = hiltViewModel()
) {

    var phoneNumber by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var isErrorLabelVisible by remember { mutableStateOf(false) }
    var isProgressBarVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val isConfirmButtonEnabled = viewModel.isConfirmButtonEnabled.collectAsState()
    val isOTPSent = viewModel.isOtpSent.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    if (isProgressBarVisible) {

        if (isOTPSent.value == false) {
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
    ) {

        item {
            Text(
                text = stringResource(id = R.string.login_number_title),
                textAlign = TextAlign.Center
            )
        }

        item {
            AutoFocusedCustomTextField(
                backgroundColor = AppColors.TelegramBlue.toColor(),
                value = phoneNumber,
                placeholder = "+391234567890",
                isError = isErrorLabelVisible,
                errorText = errorText,
                keyboardType = KeyboardType.Phone,
                onValueChange = {
                    phoneNumber = it
                    viewModel.userHasUpdatedPhoneNumber(it)
                },
                focusRequester = focusRequester
            )
        }

        item {
            TextButton(
                enabled = isConfirmButtonEnabled.value == true,
                onClick = {
                    isProgressBarVisible = true
                    coroutineScope.launch {
                        viewModel.sendOTP()
                    }
                },
                title = stringResource(id = R.string.login_number_confirm_button)
            )
        }
    }

    if (isOTPSent.value == true) {
        isProgressBarVisible = false
        navController.navigate(Section.LoginOtpVerification.baseRoute)
    } else if (isOTPSent.value == false) {
        isProgressBarVisible = false
        errorText = stringResource(R.string.login_auth_phone_number_otp_sent_failed)
        isErrorLabelVisible = true
    }
}