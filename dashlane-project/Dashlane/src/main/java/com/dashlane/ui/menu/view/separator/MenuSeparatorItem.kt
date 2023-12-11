package com.dashlane.ui.menu.view.separator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun MenuSeparatorItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
            .background(color = DashlaneTheme.colors.borderNeutralQuietIdle)
    )
}

@Preview
@Composable
private fun MenuSeparatorItemPreview() {
    DashlanePreview {
        MenuSeparatorItem()
    }
}
