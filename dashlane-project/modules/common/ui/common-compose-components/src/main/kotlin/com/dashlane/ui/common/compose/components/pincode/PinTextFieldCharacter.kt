package com.dashlane.ui.common.compose.components.pincode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun PinTextFieldCharacter(
    modifier: Modifier = Modifier,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(2.dp)
                .background(color)
        )
    }
}

@Preview
@Composable
private fun PinTextFieldCharacterPreview() {
    DashlanePreview {
        PinTextFieldCharacter(
            modifier = Modifier.size(64.dp),
            color = Color.Magenta
        ) {
            Text(text = "2")
        }
    }
}