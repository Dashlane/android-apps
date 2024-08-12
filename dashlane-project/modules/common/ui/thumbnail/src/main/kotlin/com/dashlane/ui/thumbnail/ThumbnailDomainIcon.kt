package com.dashlane.ui.thumbnail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
