package com.feduss.telegramwear.view.chat

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.view.login.MainActivityViewController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import com.google.android.horologist.compose.navscaffold.scrollable
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ChatNavView(
    activity: MainActivityViewController,
    startDestination: String = Section.ChatList.baseRoute
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

        scrollable(route = Section.ChatList.baseRoute) {
            ChatList(
                activity = activity,
                navController = navController,
                columnState = it.columnState
            )
        }
    }
}