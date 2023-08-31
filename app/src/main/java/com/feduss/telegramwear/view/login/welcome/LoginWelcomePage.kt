package com.feduss.telegramwear.view.login.welcome

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors
import com.feduss.telegramwear.viewmodel.login.welcome.LoginWelcomePageViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginWelcomePage(
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: LoginWelcomePageViewModel = hiltViewModel()
) {

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        columnState = columnState
    ) {

        item {
            Icon(
                modifier = Modifier.height(48.dp),
                painter = painterResource(id = R.drawable.ic_splashscreen_background),
                contentDescription = stringResource(id = R.string.welcome_title_icon_desc),
                tint = Color.Unspecified
            )
        }

        item {
            Text(
                text = stringResource(id = R.string.welcome_title),
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = stringResource(id = R.string.welcome_subtitle),
                textAlign = TextAlign.Center
            )
        }

        item {
            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        item {
            Button(
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppColors.TelegramBlue.toColor()
                ),
                onClick = {
                    navController.navigate(Section.LoginAuthChoice.baseRoute)
                }
            ) {
                Text(
                    text = stringResource(id = R.string.welcome_button),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}