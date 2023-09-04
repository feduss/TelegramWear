package com.feduss.telegramwear.view.component.card.chatlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Text
import com.feduss.telegram.entity.model.ChatItemModel
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    model: ChatItemModel,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {

            val titleColor =
                if (model.unreadMessagesCount == 0 || model.isMuted) {
                    Color.White
                } else {
                    AppColors.TelegramBlue.toColor()
                }

            val unreadMessagesCountColor =
                if (model.unreadMessagesCount == 0) {
                    Color.White
                } else if (model.isMuted) {
                    Color.Gray
                } else {
                    AppColors.TelegramBlue.toColor()
                }


            ConstraintLayout {
                val (profilePhoto, iconPin, onlineStatus) = createRefs()
                Image(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .constrainAs(profilePhoto) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    bitmap = model.image.asImageBitmap(),
                    contentDescription = "${model.personName} profile picture"
                )

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (model.isOnline) {
                                Color.Green
                            } else {
                                Color.LightGray
                            }
                        )
                        .constrainAs(onlineStatus) {
                            absoluteLeft.linkTo(profilePhoto.absoluteLeft, margin = (-4).dp)
                            bottom.linkTo(profilePhoto.bottom, margin = 2.dp)
                        }
                )

                if (model.isPinned) {
                    Image(
                        modifier = Modifier
                            .height(8.dp)
                            .constrainAs(iconPin) {
                                absoluteRight.linkTo(profilePhoto.absoluteRight, margin = (-4).dp)
                                bottom.linkTo(profilePhoto.bottom)
                            }
                        ,
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_pin),
                        contentDescription = "ic_pin"
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
                ) {
                    Text(
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(),
                        text = model.personName,
                        fontSize = TextUnit(12f, TextUnitType.Sp),
                        color = titleColor
                    )

                    Text(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        maxLines = 1,
                        text = model.lastMessageDate,
                        color = titleColor,
                        fontSize = TextUnit(8f, TextUnitType.Sp),
                        textAlign = TextAlign.End
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    val lastMessageImage = model.lastMessageImage
                    if (lastMessageImage != null) {
                        Image(
                            modifier = Modifier
                                .fillMaxHeight(),
                            bitmap = lastMessageImage.asImageBitmap(),
                            contentDescription = "file thumbnail"
                        )
                    }

                    Text(
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(),
                        text = model.lastMessage,
                        fontSize = TextUnit(10f, TextUnitType.Sp),
                        overflow = TextOverflow.Ellipsis
                    )

                    if (model.isMuted) {
                        Image(
                            modifier = Modifier.width(8.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_volume_mute),
                            contentDescription = "ic_volume_mute"
                        )
                    }

                    if (model.unreadMessagesCount > 0) {
                        val unreadMessagesCountStr: String =
                            if (model.unreadMessagesCount > 99) {
                                "99+"
                            } else {
                                model.unreadMessagesCount.toString()
                            }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(unreadMessagesCountColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadMessagesCountStr,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                fontSize = TextUnit(6f, TextUnitType.Sp)
                            )
                        }
                    }
                }
            }
        }
    }
}