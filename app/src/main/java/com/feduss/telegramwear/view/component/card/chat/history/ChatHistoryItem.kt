package com.feduss.telegramwear.view.component.card.chat.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Text
import com.feduss.telegram.entity.consts.ChatHistoryMessageType
import com.feduss.telegram.entity.model.ChatHistoryItemUiModel
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors
import java.time.format.DateTimeFormatter

@Composable
fun ChatHistoryItem(
    model: ChatHistoryItemUiModel,
    cardTintColor: Color = AppColors.TelegramBlue.toColor(),
    horizontalAlignment: Alignment.Horizontal,
    onCardClick: () -> Unit = {},
    onDownloadTapped: (String) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundPainter = CardDefaults.cardBackgroundPainter(
                startBackgroundColor = cardTintColor,
                endBackgroundColor = cardTintColor
            ),
            onClick = onCardClick
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MessageInfo(
                    model = model,
                    horizontalAlignment = horizontalAlignment,
                    quotedCardTintColor = AppColors.QuotedBlue.toColor()
                )
                MessageBody(
                    model = model,
                    onDownloadTapped = onDownloadTapped
                )
            }
        }
        AuthorInfo(
            model = model,
            horizontalAlignment = horizontalAlignment
        )
    }
}

@Composable
private fun AuthorInfo(
    model: ChatHistoryItemUiModel,
    horizontalAlignment: Alignment.Horizontal
) {

    val date = model.date.format(
        DateTimeFormatter.ofPattern("HH:mm")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, horizontalAlignment)
    ) {

        if (horizontalAlignment == Alignment.Start) {
            Text(
                text = model.author?.name ?: "",
                textAlign = TextAlign.Center
            )
            Text(
                text = date,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = date,
                textAlign = TextAlign.Center
            )
            Text(
                text = model.author?.name ?: "",
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun MessageInfo(
    model: ChatHistoryItemUiModel,
    horizontalAlignment: Alignment.Horizontal,
    quotedCardTintColor: Color
) {

    val textAlign: TextAlign = if (horizontalAlignment == Alignment.Start)
        TextAlign.Start
    else TextAlign.End

    if (model.forwardAuthor != null) {

        Text(
            text = "Inoltrato da ${model.forwardAuthor}",
            textAlign = textAlign,
            fontStyle = FontStyle.Italic
        )
    }

    if (model.quotedMessage != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundPainter = CardDefaults.cardBackgroundPainter(
                startBackgroundColor = quotedCardTintColor,
                endBackgroundColor = quotedCardTintColor
            ),
            onClick = {}
        ) {
            QuotedMessageBody(
                model = model,
                horizontalAlignment = horizontalAlignment,
                textAlign = textAlign
            )
        }
    }
}

@Composable
private fun QuotedMessageBody(
    model: ChatHistoryItemUiModel, horizontalAlignment: Alignment.Horizontal,
    textAlign: TextAlign) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = model.quotedMessageAuthor?.name ?: "",
            textAlign = textAlign
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = model.quotedMessage?.getText() ?: "",
                textAlign = textAlign,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Composable
private fun MessageBody(model: ChatHistoryItemUiModel, onDownloadTapped: (String) -> Unit) {

    val playButtonIconId = R.drawable.ic_play
    val playButtonIconDescription = "ic_play"

    val downloadButtonIconId = R.drawable.ic_download_arrow
    val downloadButtonIconDescription = "ic_download_arrow"

    when (val messageType = model.type) {
        is ChatHistoryMessageType.Text -> {
            Text(
                text = messageType.message,
                textAlign = TextAlign.Center
            )
        }

        is ChatHistoryMessageType.Audio -> {
            val isRead = model.isRead
            val iconColor = if (isRead) {
                Color.Cyan
            } else {
                Color.White
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {

                val buttonIconId: Int
                val buttonIconContentDescription: String
                val buttonAction: (String) -> Unit

                if (messageType.filePath != null) {
                    buttonIconId = playButtonIconId
                    buttonIconContentDescription = playButtonIconDescription
                    buttonAction = {} //TODO: to impl
                } else {
                    buttonIconId = downloadButtonIconId
                    buttonIconContentDescription = downloadButtonIconDescription
                    buttonAction = onDownloadTapped
                }

                CompactButton(
                    modifier = Modifier
                        .size(32.dp),
                    onClick = { buttonAction(messageType.remoteId) }) {


                    Image(
                        imageVector = ImageVector.vectorResource(id = buttonIconId),
                        contentDescription = buttonIconContentDescription
                    )
                }

                val formattedDuration: String = if(messageType.duration < 60) {
                    if (messageType.duration < 10) {
                        "00:0${messageType.duration}"
                    } else {
                        "00:${messageType.duration}"
                    }
                } else {
                    val minutes = messageType.duration / 60
                    val seconds = messageType.duration % 60

                    "$minutes:$seconds"
                }

                Text(
                    text = formattedDuration,
                    textAlign = TextAlign.Center
                )
            }
        }

        is ChatHistoryMessageType.Video -> {

            //TODO: to impl
        }

        is ChatHistoryMessageType.Photo -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConstraintLayout {
                    val (photo, downloadButton) = createRefs()
                    val bitmap = (messageType.photoBitmap ?: messageType.thumbnail).asImageBitmap()
                    val computedAspectRatio = (bitmap.width / bitmap.height).toFloat()
                    val aspectRatio = if (computedAspectRatio > 0.0) computedAspectRatio else 1.0f
                    Image(
                        modifier = Modifier
                            .aspectRatio(aspectRatio)
                            .fillMaxSize()
                            .constrainAs(photo) {
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            },
                        bitmap = bitmap,
                        contentDescription = "image"
                    )

                    if (messageType.photoBitmap == null) {
                        CompactButton(
                            modifier = Modifier
                                .size(32.dp)
                                .constrainAs(downloadButton) {
                                    centerTo(photo)
                                },
                            onClick = { onDownloadTapped(messageType.remoteId) }
                        ) {
                            Image(
                                imageVector = ImageVector.vectorResource(id = downloadButtonIconId),
                                contentDescription = downloadButtonIconDescription
                            )
                        }
                    }
                }
                if (!messageType.caption.isEmpty()) {
                    Text(
                        text = messageType.caption,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is ChatHistoryMessageType.Document -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                /*val icon = messageType.icon
                if (icon != null) {
                    Image(
                        modifier = Modifier
                            .height(8.dp),
                        bitmap = icon.asImageBitmap(),
                        contentDescription = "icon document"
                    )
                }*/
                Text(
                    text = messageType.fileName,
                    textAlign = TextAlign.Center
                )

                val buttonIconId: Int
                val buttonIconContentDescription: String
                val buttonAction: () -> Void //TODO: to impl

                if (messageType.filePath == null) {
                    buttonIconId = R.drawable.ic_download_arrow
                    buttonIconContentDescription = "ic_download_arrow"

                    CompactButton(
                        modifier = Modifier
                            .height(8.dp),
                        onClick = { /*TODO*/ }) {
                        Image(
                            modifier = Modifier
                                .height(8.dp),
                            imageVector = ImageVector.vectorResource(id = buttonIconId),
                            contentDescription = buttonIconContentDescription
                        )


                    }
                }
            }
        }

        is ChatHistoryMessageType.Other -> {
            Text(
                text = stringResource(R.string.chat_history_not_supported_message),
                textAlign = TextAlign.Center
            )
        }
    }
}