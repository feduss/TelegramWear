package com.feduss.telegramwear.view.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.feduss.telegramwear.colors.AppColors

@Composable
fun TextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    title: String
) {
    Button(
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppColors.TelegramBlue.toColor()
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center
        )
    }
}