package com.feduss.telegramwear.view.component.card.chatlist

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    image: Bitmap,
    personName: String,
    lastMessageImage: Bitmap?,
    lastMessage: String,
    cardTintColor: Color = Color.Black,
    onCardClick: () -> Unit = {}
) {

    Card(
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = cardTintColor,
            endBackgroundColor = cardTintColor
        ),
        onClick = onCardClick
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(cardTintColor, RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Image(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                bitmap = image.asImageBitmap(),
                contentDescription = "${personName} profile picture"
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = personName,
                    fontSize = TextUnit(12f, TextUnitType.Sp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lastMessageImage != null) {
                        Image(
                            modifier = Modifier
                                .fillMaxHeight()
                                .basicMarquee(),
                            bitmap = lastMessageImage.asImageBitmap(),
                            contentDescription = "file thumbnail"
                        )
                    }
                    Text(
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(),
                        text = lastMessage,
                        fontSize = TextUnit(10f, TextUnitType.Sp),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}