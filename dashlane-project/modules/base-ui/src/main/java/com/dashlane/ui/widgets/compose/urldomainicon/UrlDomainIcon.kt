package com.dashlane.ui.widgets.compose.urldomainicon

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.url.UrlDomain
import com.dashlane.url.icon.UrlDomainIcon
import com.dashlane.url.icon.UrlDomainIconColor
import com.dashlane.url.icon.toColorIntOrNull
import kotlinx.coroutines.async
import java.util.Locale

@Composable
fun UrlDomainIcon(
    urlDomain: UrlDomain?,
    modifier: Modifier = Modifier,
    issuer: String? = null,
    viewModel: UrlDomainIconViewModel = viewModel(key = "domain:${urlDomain?.value},issuer:$issuer")
) {
    val urlDomainIcon by viewModel.urlDomainIcon.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(key1 = viewModel) {
        viewModel.fetchIcon(urlDomain)
    }
    UrlDomainIcon(
        urlDomainIcon = urlDomainIcon,
        modifier = modifier,
        placeholderText = issuer ?: urlDomain?.displayName
    )
}

@Suppress("LongMethod")
@Composable
fun UrlDomainIcon(
    urlDomainIcon: UrlDomainIcon?,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
) {
    val shape = RoundedCornerShape(4.dp)

    SubcomposeAsyncImage(
        modifier = modifier
            .clip(shape)
            .border(1.dp, DashlaneTheme.colors.borderNeutralQuietIdle, shape),
        imageLoader = ImageLoader.Builder(LocalContext.current.applicationContext).build(),
        model = ImageRequest.Builder(LocalContext.current)
            .data(urlDomainIcon?.url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit
    ) {
        val defaultBackgroundColor = DashlaneTheme.colors.containerAgnosticNeutralQuiet
        val defaultContentColor = DashlaneTheme.colors.textBrandStandard.value

        val backgroundColor = remember { Animatable(defaultBackgroundColor) }
        val contentColor = remember { Animatable(defaultContentColor) }

        LaunchedEffect(painter.state, urlDomainIcon) {
            val urlDomainIconBackgroundColor = urlDomainIcon?.colors?.background
                ?.toColorIntOrNull()
                ?.let(::Color)
            val urlDomainIconContentColor = urlDomainIcon?.colors?.main
                ?.toColorIntOrNull()
                ?.let(::Color)

            val useUrlDomainIconColors =
                (painter.state is AsyncImagePainter.State.Error || painter.state is AsyncImagePainter.State.Success) &&
                    urlDomainIconBackgroundColor != null &&
                    urlDomainIconContentColor != null

            val animSpec = tween<Color>(durationMillis = 100, easing = LinearEasing)

            val backgroundAnim = async {
                backgroundColor.animateTo(
                    targetValue = urlDomainIconBackgroundColor.takeIf { useUrlDomainIconColors }
                        ?: defaultBackgroundColor,
                    animationSpec = animSpec
                )
            }

            val contentAnim = async {
                contentColor.animateTo(
                    targetValue = urlDomainIconContentColor.takeIf { useUrlDomainIconColors }
                        ?: defaultContentColor,
                    animationSpec = animSpec
                )
            }

            contentAnim.await()
            backgroundAnim.await()
        }

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.value)
        )

        if (this@SubcomposeAsyncImage.painter.state is AsyncImagePainter.State.Success) {
            this@SubcomposeAsyncImage.SubcomposeAsyncImageContent()
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = placeholderText?.placeholderText ?: "?",
                    modifier = Modifier.padding(4.dp),
                    style = LocalTextStyle.current
                        .merge(
                            TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor.value
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
@Composable
fun previewPlaceHolderUrlDomainIcon() {
    DashlanePreview {
        UrlDomainIcon(
            urlDomainIcon = UrlDomainIcon.Placeholder(
                domain = "test.com",
                colors = UrlDomainIcon.Colors(
                    main = UrlDomainIconColor("0xFF0000"),
                    background = UrlDomainIconColor("0x00FF00"),
                    fallback = UrlDomainIconColor("0x0000FF")
                )
            ),
            modifier = Modifier.size(62.dp, 34.dp),
            placeholderText = "Placeholder"
        )
    }
}