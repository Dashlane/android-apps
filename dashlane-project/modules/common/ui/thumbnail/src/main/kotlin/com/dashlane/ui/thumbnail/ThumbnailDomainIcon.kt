package com.dashlane.ui.thumbnail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType

@Composable
fun ThumbnailDomainIcon(
    urlDomain: String?,
    modifier: Modifier = Modifier,
    size: ThumbnailSize = ThumbnailSize.Medium,
) {
    if (LocalInspectionMode.current) {
        Thumbnail(
            modifier = modifier,
            type = ThumbnailType.VaultItem.DomainIcon(),
            size = size,
        )
        return
    }

    BaseThumbnailDomainIcon(
        modifier = modifier,
        urlDomain = urlDomain,
        size = size,
    )
}

@Composable
private fun BaseThumbnailDomainIcon(
    modifier: Modifier,
    urlDomain: String?,
    size: ThumbnailSize,
    viewModel: ThumbnailDomainIconViewModel = viewModel<ThumbnailDomainIconViewModel>(key = "domain:$urlDomain")
) {
    val urlDomainIcon by viewModel.urlDomainIcon.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(viewModel) {
        viewModel.fetchIcon(urlDomain)
    }

    Thumbnail(
        modifier = modifier,
        type = ThumbnailType.VaultItem.DomainIcon(
            url = urlDomainIcon?.url,
            containerColor = urlDomainIcon?.color,
        ),
        size = size,
    )
}
