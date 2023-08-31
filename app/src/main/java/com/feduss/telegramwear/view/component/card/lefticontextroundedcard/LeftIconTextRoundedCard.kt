package com.feduss.telegramwear.view.component.card.lefticontextroundedcard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.feduss.telegramwear.colors.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeftIconTextRoundedCard(
    text: String,
    leftIconId: Int,
    leftIconContentDescription: String,
    cardTintColor: Color = AppColors.TelegramBlue.toColor(),
    iconTintColor: Color = Color.White,
    onCardClick: () -> Unit = {}
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = cardTintColor,
            endBackgroundColor = cardTintColor
        ),
        onClick = onCardClick
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Icon(
                modifier = Modifier.width(24.dp),
                imageVector = ImageVector.vectorResource(
                    id = leftIconId
                ),
                contentDescription = leftIconContentDescription,
                tint = iconTintColor
            )

            Text(
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(),
                text = text
            )
        }
    }
}