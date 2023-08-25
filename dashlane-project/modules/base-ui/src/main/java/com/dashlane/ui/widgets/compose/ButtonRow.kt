package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity

@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    textPrimary: String,
    textSecondary: String,
    onClickPrimary: () -> Unit,
    onClickSecondary: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        ButtonMedium(
            modifier = Modifier.padding(end = 16.dp),
            onClick = onClickSecondary,
            intensity = Intensity.Quiet,
            layout = ButtonLayout.TextOnly(text = textSecondary)
        )
        ButtonMedium(
            onClick = onClickPrimary,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(text = textPrimary)
        )
    }
}

@Preview
@Composable
fun ButtonRowPreview() {
    DashlaneTheme {
        ButtonRow(
            textPrimary = "Primary",
            textSecondary = "Secondary",
            onClickPrimary = {},
            onClickSecondary = {}
        )
    }
}
