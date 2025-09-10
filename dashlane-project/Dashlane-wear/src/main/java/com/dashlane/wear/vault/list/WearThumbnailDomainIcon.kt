package com.dashlane.wear.vault.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.DecorativeColor
import com.dashlane.ui.thumbnail.ThumbnailDomainIconViewModel
import com.dashlane.wear.ui.component.WearIcon

@Composable
fun WearThumbnailDomainIcon(
    urlDomain: String?,
    isPasskey: Boolean,
    modifier: Modifier = Modifier,
    size: ThumbnailSize = ThumbnailSize.Small,
    viewModel: ThumbnailDomainIconViewModel = hiltViewModel<ThumbnailDomainIconViewModel>(key = "domain:$urlDomain"),
) {
    val urlDomainIcon by viewModel.urlDomainIcon.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(viewModel) {
        viewModel.fetchIcon(urlDomain)
    }
    if (isPasskey) {
        Box {
            Thumbnail(
                modifier = modifier,
                type = ThumbnailType.VaultItem.DomainIcon(
                    url = urlDomainIcon?.url,
                    containerColor = urlDomainIcon?.color,
                ),
                size = size,
            )
            WearIcon(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 3.dp),
                token = IconTokens.passkeyOutlined,
                contentDescription = "",
            )
        }
    } else {
        Thumbnail(
            modifier = modifier,
            type = ThumbnailType.VaultItem.DomainIcon(
                url = urlDomainIcon?.url,
                containerColor = urlDomainIcon?.color,
            ),
            size = size,
        )
    }
}

@Composable
fun WearThumbnailDomainIconForPreview(modifier: Modifier = Modifier, isPasskey: Boolean) {
    if (isPasskey) {
        Box {
            Thumbnail(
                modifier = modifier,
                type = ThumbnailType.VaultItem.OtherIcon(
                    token = IconTokens.homeOutlined,
                    color = DecorativeColor.BLACK,
                ),
                size = ThumbnailSize.Small,
            )
            WearIcon(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 3.dp),
                token = IconTokens.passkeyOutlined,
                contentDescription = "",
            )
        }
    } else {
        Thumbnail(
            modifier = modifier,
            type = ThumbnailType.VaultItem.OtherIcon(
                token = IconTokens.homeOutlined,
                color = DecorativeColor.BLACK,
            ),
            size = ThumbnailSize.Small,
        )
    }
}

@Composable
fun WearMiniDomainIcon(
    urlDomain: String?,
    modifier: Modifier = Modifier,
    viewModel: ThumbnailDomainIconViewModel = hiltViewModel<ThumbnailDomainIconViewModel>(key = "domain:$urlDomain"),
) {
    Box(modifier = modifier.size(20.dp)) {
        
        WearThumbnailDomainIcon(urlDomain = urlDomain, isPasskey = false, viewModel = viewModel)
    }
}

@Composable
fun WearMiniDomainIconForPreview(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(20.dp)) {
        
        WearThumbnailDomainIconForPreview(isPasskey = false)
    }
}