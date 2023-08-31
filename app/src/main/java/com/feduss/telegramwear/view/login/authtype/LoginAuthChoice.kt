package com.feduss.telegramwear.view.login.authtype

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.R
import com.feduss.telegramwear.view.component.card.lefticontextroundedcard.LeftIconTextRoundedCard
import com.feduss.telegramwear.viewmodel.login.authtype.LoginAuthChoiceViewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LoginAuthChoice(
    navController: NavHostController,
    columnState: ScalingLazyColumnState,
    viewModel: LoginAuthChoiceViewModel = hiltViewModel()
) {

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        columnState = columnState
    ) {

        item {
            Text(
                text = stringResource(id = R.string.login_auth_title),
                textAlign = TextAlign.Center
            )
        }

        item {
            LeftIconTextRoundedCard(
                text = stringResource(id = R.string.login_auth_qrcode),
                leftIconId = R.drawable.ic_qr_code,
                leftIconContentDescription = "ic_qr_code",
                onCardClick = {
                    navController.navigate(Section.LoginAuthQrCode.baseRoute)
                }
            )
        }

        item {
            LeftIconTextRoundedCard(
                text = stringResource(id = R.string.login_auth_phone_number),
                leftIconId = R.drawable.ic_phone,
                leftIconContentDescription = "ic_phone",
                onCardClick = {
                    navController.navigate(Section.LoginAuthPhoneNumber.baseRoute)
                }
            )
        }
    }
}