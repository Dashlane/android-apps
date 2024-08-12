package com.dashlane.premium.offer.details.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.HtmlText
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun BenefitRow(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressiveIcon(
            icon = IconTokens.checkmarkOutlined,
            size = ExpressiveIconSize.Small,
        )
        HtmlText(
            modifier = Modifier.padding(start = 16.dp),
            htmlText = text,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard
        )
    }
}

@Preview
@Composable
private fun BenefitRowPreview() = DashlanePreview {
    BenefitRow(modifier = Modifier.padding(8.dp), text = "Unlimited passwords!")
}
