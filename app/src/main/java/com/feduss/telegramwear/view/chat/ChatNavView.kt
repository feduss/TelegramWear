package com.feduss.telegramwear.view.chat

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.feduss.telegram.entity.consts.Params
import com.feduss.telegram.entity.consts.Section
import com.feduss.telegramwear.factory.getChatHistoryViewModel
import com.feduss.telegramwear.view.MainActivityViewController
import com.feduss.telegramwear.view.component.PageView
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.scrollable

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ChatNavView(
    activity: MainActivityViewController,
    startDestination: String = Section.ChatList.baseRoute
) {

    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        modifier = Modifier.background(Color.Black),
        navController = navController,
        startDestination = startDestination
    ) {

        scrollable(route = Section.ChatList.baseRoute) {
            PageView {
                ChatList(
                    activity = activity,
                    navController = navController,
                    columnState = it
                )
            }
        }

        scrollable(
            route = Section.ChatHistory.parametricRoute,
            arguments = listOf(
                navArgument(Params.ChatId.name) { type = NavType.StringType }
            )
        ) { scrollableScaffoldContext ->
            val chatId: String =
                scrollableScaffoldContext.arguments?.getString(Params.ChatId.name) ?: ""

            val profilePhoto = navController.previousBackStackEntry?.savedStateHandle?.get<Bitmap>("profilePhoto")
            val profileName = navController.previousBackStackEntry?.savedStateHandle?.get<String>("profileName")

            if (profilePhoto != null && profileName != null) {
                PageView {
                    ChatHistory(
                        activity = activity,
                        navController = navController,
                        columnState = it,
                        profilePhoto = profilePhoto,
                        profileName = profileName,
                        viewModel = getChatHistoryViewModel(
                            activity = activity,
                            chatId = chatId
                        )
                    )
                }
            }
        }
    }
}