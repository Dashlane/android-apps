package com.dashlane.ui.widgets.compose.pincode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme

@Suppress("LongMethod")
@Composable
fun PinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = FocusRequester()

        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .clickable { },
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            decorationBox = {
                Row {
                    repeat(4) { i ->
                        val hasChar = i < value.length
                        PinTextFieldCharacter(
                            color = when {
                                isError -> DashlaneTheme.colors.borderDangerStandardIdle
                                hasChar -> DashlaneTheme.colors.borderBrandStandardActive
                                else -> DashlaneTheme.colors.borderNeutralStandardIdle
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            if (hasChar) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .size(16.dp)
                                        .background(
                                            DashlaneTheme.colors.textBrandQuiet.value,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        )

        Text(
            modifier = Modifier
                .width(280.dp)
                .padding(top = 8.dp)
                .padding(horizontal = 4.dp),
            text = errorMessage ?: "",
            style = DashlaneTheme.typography.bodyHelperRegular,
            color = DashlaneTheme.colors.textDangerQuiet
        )
    }
}