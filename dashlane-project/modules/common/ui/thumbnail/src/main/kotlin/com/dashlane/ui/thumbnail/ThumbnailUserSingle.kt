package com.dashlane.ui.thumbnail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.util.MD5Hash

@Composable
fun ThumbnailUserSingle(
    modifier: Modifier = Modifier,
    email: String? = null,
    size: ThumbnailSize = ThumbnailSize.Medium,
) {
    val url = remember(email) {
        "https://www.gravatar.com/avatar/${MD5Hash.hash(email)}?s=200&r=pg&d=404"
    }
    Thumbnail(
        modifier = modifier,
        type = ThumbnailType.User.Single(
            url = url
        ),
        size = size
    )
}