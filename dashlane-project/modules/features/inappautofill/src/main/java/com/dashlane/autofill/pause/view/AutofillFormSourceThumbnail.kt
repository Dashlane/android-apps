package com.dashlane.autofill.pause.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.api.R
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon
import com.dashlane.util.toBitmap
import com.dashlane.util.tryOrNull

@Composable
internal fun AutofillFormSourceThumbnail(
    icon: AutofillFormSourceIcon,
    modifier: Modifier = Modifier,
) {
    when (icon) {
        is AutofillFormSourceIcon.InstalledApplication -> {
            val context = LocalContext.current
            val imageBitmap: ImageBitmap? = remember(icon.applicationInfo) {
                tryOrNull {
                    context.packageManager.getApplicationIcon(icon.applicationInfo)
                        .toBitmap()
                        .asImageBitmap()
                }
            }
            if (imageBitmap != null) {
                val thumbnailDescription = getAppThumbnailDescription()
                Image(
                    modifier = modifier
                        .size(40.dp)
                        .semantics { contentDescription = thumbnailDescription },
                    bitmap = imageBitmap,
                    contentDescription = null,
                )
            } else {
                AppThumbnailFallback(modifier = modifier)
            }
        }

        is AutofillFormSourceIcon.NotInstalledApplication -> {
            if (!icon.mainDomain.isNullOrBlank()) {
                val thumbnailDescription = getAppThumbnailDescription()
                ThumbnailDomainIcon(
                    modifier = modifier.semantics { contentDescription = thumbnailDescription },
                    urlDomain = icon.mainDomain,
                )
            } else {
                AppThumbnailFallback(modifier = modifier)
            }
        }

        AutofillFormSourceIcon.UnknownApplication -> {
            AppThumbnailFallback(modifier = modifier)
        }

        is AutofillFormSourceIcon.WebsiteUrl -> {
            val thumbnailDescription = getWebsiteThumbnailDescription()
            ThumbnailDomainIcon(
                modifier = modifier.semantics { contentDescription = thumbnailDescription },
                urlDomain = icon.url,
            )
        }
    }
}

@Composable
private fun AppThumbnailFallback(modifier: Modifier = Modifier) {
    val thumbnailDescription = getAppThumbnailDescription()
    com.dashlane.design.component.Thumbnail(
        modifier = modifier.semantics {
            contentDescription = thumbnailDescription
        },
        type = ThumbnailType.Icon(
            token = IconTokens.osAndroidFilled,
        ),
        size = ThumbnailSize.Medium,
    )
}

@Composable
private fun getAppThumbnailDescription() = stringResource(R.string.and_accessibility_list_item_thumbnail_app)

@Composable
private fun getWebsiteThumbnailDescription() = stringResource(R.string.and_accessibility_list_item_thumbnail_website)

@Preview
@Composable
private fun AppThumbnailFallbackPreview() {
    DashlanePreview {
        AppThumbnailFallback()
    }
}