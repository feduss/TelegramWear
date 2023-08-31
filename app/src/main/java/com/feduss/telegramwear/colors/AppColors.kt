package com.feduss.telegramwear.colors

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

enum class AppColors {
    TelegramBlue,
    DarkGray;

    fun toColor(): Color {
        return when(this) {
            TelegramBlue -> Color("#039ae3".toColorInt())
            DarkGray -> Color("#232323".toColorInt())
        }
    }
}