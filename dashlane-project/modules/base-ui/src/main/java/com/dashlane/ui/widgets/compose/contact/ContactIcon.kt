package com.dashlane.ui.widgets.compose.contact

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.util.MD5Hash.hash
import java.util.Locale

@Composable
fun ContactIcon(email: String, modifier: Modifier = Modifier) {
    val url = "https://www.gravatar.com/avatar/${hash(email)}?s=200&r=pg&d=404"
    SubcomposeAsyncImage(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, DashlaneTheme.colors.borderNeutralQuietIdle, CircleShape),
        imageLoader = ImageLoader.Builder(LocalContext.current.applicationContext).build(),
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit
    ) {
        if (this@SubcomposeAsyncImage.painter.state is AsyncImagePainter.State.Success) {
            this@SubcomposeAsyncImage.SubcomposeAsyncImageContent()
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = email.placeholderText ?: "?",
                    modifier = Modifier.padding(4.dp),
                    style = LocalTextStyle.current
                        .merge(
                            TextStyle(
                                fontSize = 3.6.em,
                                fontWeight = FontWeight.SemiBold,
                                color = DashlaneTheme.colors.textBrandStandard.value
                            )
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

private val String.placeholderText
    get() = trim { it < ' ' }
        .take(2)
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
        .trim { it < ' ' }
        .takeUnless { it.isBlank() }

@Preview
@Preview(name = "small font", fontScale = 0.5f)
@Preview(name = "large font", fontScale = 1.5f)
@Composable
fun PreviewContactIcon() {
    DashlanePreview {
        Column {
            ContactIcon("randomemail@provider.com", Modifier.size(36.dp))
            ContactIcon("randomemail@provider.com", Modifier.size(48.dp))
            ContactIcon("randomemail@provider.com", Modifier.size(56.dp))
        }
    }
}