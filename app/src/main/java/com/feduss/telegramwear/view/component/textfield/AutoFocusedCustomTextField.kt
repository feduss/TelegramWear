package com.feduss.telegramwear.view.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.Text
import com.feduss.telegramwear.R
import com.feduss.telegramwear.colors.AppColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutoFocusedCustomTextField(
    backgroundColor: Color,
    value: String,
    placeholder: String? = null,
    isError: Boolean,
    errorText: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .focusRequester(focusRequester),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (placeholder?.isNotEmpty() == true && value.isEmpty()) {
                        Text(
                            text = placeholder,
                            textAlign = TextAlign.Center,
                            color = AppColors.DarkGray.toColor(),
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }

            }
        )

        if (isError) {
            Text(
                text = errorText,
                textAlign = TextAlign.Center,
                color = Color.Red
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.hide()
    }
}