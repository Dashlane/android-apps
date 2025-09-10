package com.dashlane.wear.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.wear.design.compose.R
import com.dashlane.wear.ui.DashlaneWearPreview

@Composable
fun ObfuscatedOtpField(
    code: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(code.length) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset()
                    .background(
                        shape = CircleShape,
                        color = DashlaneTheme.colors.textNeutralQuiet.value,
                    ),
            )
        }
    }
}

@Composable
fun PinKeyboard(
    onInput: (Char) -> Unit,
    onDelete: () -> Unit,
    onValidate: () -> Unit,
) {
    Column {
        Row(modifier = Modifier) {
            TappableContainer(onClick = { onInput(1.digitToChar()) }) {
                KeyDigit("1")
            }
            TappableContainer(onClick = { onInput(2.digitToChar()) }) {
                KeyDigit("2")
            }
            TappableContainer(onClick = { onInput(3.digitToChar()) }) {
                KeyDigit("3")
            }
        }
        Row(modifier = Modifier) {
            TappableContainer(onClick = { onInput(4.digitToChar()) }) {
                KeyDigit("4")
            }
            TappableContainer(onClick = { onInput(5.digitToChar()) }) {
                KeyDigit("5")
            }
            TappableContainer(onClick = { onInput(6.digitToChar()) }) {
                KeyDigit("6")
            }
        }
        Row(modifier = Modifier) {
            TappableContainer(onClick = { onInput(7.digitToChar()) }) {
                KeyDigit("7")
            }
            TappableContainer(onClick = { onInput(8.digitToChar()) }) {
                KeyDigit("8")
            }
            TappableContainer(onClick = { onInput(9.digitToChar()) }) {
                KeyDigit("9")
            }
        }
        Row(modifier = Modifier) {
            TappableContainer(onClick = onDelete) {
                WearIcon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_return),
                    contentDescription = "",
                    tint = Color.White,
                )
            }
            TappableContainer(onClick = { onInput(0.digitToChar()) }) {
                KeyDigit("0")
            }
            TappableContainer(onClick = onValidate) {
                WearIcon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_caret_right_outlined),
                    contentDescription = "",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun KeyDigit(digit: String) {
    WearText(
        text = digit,
        style = DashlaneTheme.typography.bodyReducedMonospace,
        color = DashlaneTheme.colors.textInverseCatchy,
    )
}

@Preview
@Composable
private fun ObfuscatedOtpFieldPreview() {
    DashlaneWearPreview {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ObfuscatedOtpField(code = "4321")
        }
    }
}

@Preview
@Composable
private fun PinKeyboardPreview() {
    DashlaneWearPreview {
        Box(
            modifier = Modifier
                .background(color = Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            PinKeyboard(
                onInput = {},
                onDelete = {},
                onValidate = {},
            )
        }
    }
}