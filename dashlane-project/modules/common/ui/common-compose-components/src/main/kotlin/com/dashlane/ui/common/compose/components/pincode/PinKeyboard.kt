package com.dashlane.ui.common.compose.components.pincode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R

private val pinKeyboardKeySize = 72.dp

@Composable
fun PinKeyboard(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    @Composable
    fun PinKeyboardKeyDigit(
        digit: String
    ) {
        PinKeyboardKey(
            modifier = Modifier.clickable(role = Role.Button) {
                onValueChange(value + digit)
            }
        ) {
            Text(
                text = digit,
                style = DashlaneTheme.typography.specialtyMonospaceMedium
            )
        }
    }

    Column(
        modifier = modifier
    ) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            PinKeyboardKeyDigit(digit = "1")
            PinKeyboardKeyDigit(digit = "2")
            PinKeyboardKeyDigit(digit = "3")
        }

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            PinKeyboardKeyDigit(digit = "4")
            PinKeyboardKeyDigit(digit = "5")
            PinKeyboardKeyDigit(digit = "6")
        }

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            PinKeyboardKeyDigit(digit = "7")
            PinKeyboardKeyDigit(digit = "8")
            PinKeyboardKeyDigit(digit = "9")
        }

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Spacer(modifier = Modifier.size(pinKeyboardKeySize))
            PinKeyboardKeyDigit(digit = "0")
            PinKeyboardKey(
                modifier = Modifier.clickable(role = Role.Button) {
                    if (value.isNotEmpty()) {
                        onValueChange(value.dropLast(1))
                    }
                }
            ) {
                @Suppress("DEPRECATION")
                Icon(
                    painter = painterResource(R.drawable.backspace),
                    contentDescription = stringResource(id = R.string.and_accessibility_pin_keyboard_action_text_clear)
                )
            }
        }
    }
}

@Composable
private fun PinKeyboardKey(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(pinKeyboardKeySize),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
private fun PinKeyboardPreview() {
    DashlanePreview {
        PinKeyboard(
            Modifier.size(
                width = 500.dp,
                height = 400.dp
            ),
            value = "1234",
            onValueChange = {}
        )
    }
}