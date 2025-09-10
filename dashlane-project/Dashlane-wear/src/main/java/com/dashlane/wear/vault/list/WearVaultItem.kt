package com.dashlane.wear.vault.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.WearChip

@Composable
fun WearVaultItem(
    url: String,
    title: String,
    isPasskey: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: String? = null,
) {
    WearChip(
        modifier = modifier,
        icon = {
            if (!LocalInspectionMode.current) {
                WearThumbnailDomainIcon(urlDomain = url, isPasskey = isPasskey)
            } else {
                WearThumbnailDomainIconForPreview(isPasskey = isPasskey)
            }
        },
        label = title,
        secondaryLabel = content,
        emphasis = Emphasis.Low,
        onClick = onClick,
    )
}

@Composable
@Preview
private fun WearVaultItemPreview() {
    DashlaneWearPreview {
        WearVaultItem(
            url = "spotify.com",
            title = "Spotify",
            content = "randomemail@provider.com",
            isPasskey = true,
            onClick = {},
        )
    }
}