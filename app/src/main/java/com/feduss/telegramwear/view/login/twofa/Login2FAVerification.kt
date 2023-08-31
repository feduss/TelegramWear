package com.feduss.telegramwear.view.login.twofa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.feduss.telegramwear.viewmodel.login.twofa.Login2FAViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Login2FAVerification(
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: Login2FAViewModel = hiltViewModel(),
    passwordHint: String = "password hint"
) {

    var isProgressBarVisible by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val isConfirmButtonEnabled = viewModel.isConfirmButtonEnabled.collectAsState()
    val isPasswordValidated = viewModel.isPasswordValidated.collectAsState()

    if (isPasswordValidated.value == true) {
        navController.navigate(Section.ChatList.baseRoute)
    } else if (isPasswordValidated.value == false) {
        isProgressBarVisible = true
    }

    if (isProgressBarVisible) {

        if (isPasswordValidated.value == false) {
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
                text = stringResource(id = R.string.login_2fa_title),
                textAlign = TextAlign.Center
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            AutoFocusedCustomTextField(
                backgroundColor = AppColors.TelegramBlue.toColor(),
                value = password,
                placeholder = passwordHint,
                isError = isPasswordValidated.value == false,
                errorText = stringResource(id = R.string.login_2fa_errror),
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = {
                    password = it
                    viewModel.userHasEditedPassword(it)
                },
                focusRequester = focusRequester
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    enabled = isConfirmButtonEnabled.value == true,
                    onClick = {
                        isProgressBarVisible = true
                        coroutineScope.launch {
                            viewModel.checkPassword(password)
                        }
                    },
                    title = stringResource(id = R.string.login_number_confirm_button)
                )
            }
        }
    }
}