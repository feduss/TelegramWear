package com.feduss.telegramwear.view.component.card.chat.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import com.valentinilk.shimmer.shimmer

@Composable
fun ChatHistoryItemSkeleton(
    horizontalAlignment: Alignment.Horizontal
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = horizontalAlignment
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            onClick = {}
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = horizontalAlignment
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray)
                        .fillMaxWidth()
                        .height(12.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray)
                        .fillMaxWidth(0.2f)
                        .height(4.dp)
                )
            }
        }
    }


}