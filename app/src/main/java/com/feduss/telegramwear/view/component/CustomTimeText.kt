package com.feduss.telegramwear.view.component

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.curvedText
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.scrollAway
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun CustomTimeText(columnState: ScalingLazyColumnState) {
    val timeSource = TimeTextDefaults.timeSource(
        DateFormat.getBestDateTimePattern(Locale.getDefault(), "HH:mm")
    )

    val textColor = Color("#FF8133".toColorInt())

    val endCurvedText = ""

    val modifier = Modifier.scrollAway(columnState)

    if (endCurvedText.isNotEmpty()) {
        TimeText(
            modifier = modifier,
            timeSource = timeSource,
            endCurvedContent = {
                curvedText(
                    text = endCurvedText,
                    color = textColor
                )
            }
        )
    } else {
        TimeText(
            modifier = modifier,
            timeSource = timeSource,
        )
    }
}