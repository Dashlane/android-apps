package com.dashlane.ui.common.compose.components.pincode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Suppress("LongMethod")
@Composable
fun PinTextField(
    modifier: Modifier = Modifier,
    length: Int,
    value: String,
    isError: Boolean,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
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
                    repeat(length) { i ->
                        val hasChar = i < value.length
                        PinTextFieldCharacter(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .weight(0.25f)
                                .height(64.dp),
                            color = when {
                                isError -> DashlaneTheme.colors.borderDangerStandardIdle
                                hasChar -> DashlaneTheme.colors.borderBrandStandardActive
                                else -> DashlaneTheme.colors.borderNeutralStandardIdle
                            },
                        ) {
                            if (hasChar) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .size(16.dp)
                                        .background(
                                            color = when {
                                                isError -> DashlaneTheme.colors.borderDangerStandardIdle
                                                else -> DashlaneTheme.colors.borderBrandStandardActive
                                            },
                                            shape = CircleShape
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
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 4.dp),
            text = errorMessage ?: "",
            style = DashlaneTheme.typography.bodyHelperRegular,
            color = DashlaneTheme.colors.textDangerQuiet
        )
    }
}

@Preview
@Composable
private fun PinTextFieldPreview() {
    DashlanePreview {
        PinTextField(
            length = 6,
            value = "00",
            isError = true,
            errorMessage = "Error",
            onValueChange = {}
        )
    }
}
