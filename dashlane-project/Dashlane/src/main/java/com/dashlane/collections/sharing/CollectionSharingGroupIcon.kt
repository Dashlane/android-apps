package com.dashlane.collections.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Icon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun CollectionSharingGroupIcon() {
    Icon(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = DashlaneTheme.colors.containerAgnosticNeutralStandard,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = DashlaneTheme.colors.borderNeutralQuietIdle,
                shape = CircleShape
            )
            .padding(all = 10.dp),
        token = IconTokens.groupOutlined,
        contentDescription = null,
        tint = DashlaneTheme.colors.textBrandQuiet
    )
}

@Composable
@Preview
fun CollectionSharingGroupIconPreview() {
    DashlanePreview {
        CollectionSharingGroupIcon()
    }
}