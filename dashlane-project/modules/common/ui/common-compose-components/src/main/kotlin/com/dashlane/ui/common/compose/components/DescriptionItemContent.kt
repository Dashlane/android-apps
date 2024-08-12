package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun DescriptionItemContent(iconToken: IconToken, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressiveIcon(icon = iconToken)
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard
        )
    }
}

@Preview
@Composable
private fun DescriptionItemContentPreview() {
    DashlanePreview {
        DescriptionItemContent(
            iconToken = IconTokens.itemPhoneMobileOutlined,
            title = "Use an Android phone for the best Dashlane experience"
        )
    }
}