package com.dashlane.ui.activities.intro

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Icon
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R
import com.dashlane.ui.widgets.compose.basescreen.NoAppBarScreenWrapper

data class DescriptionItem(val imageIconToken: IconToken, @StringRes val titleResId: Int = 0)
sealed class LinkItem {
    abstract val linkResId: Int

    data class ExternalLinkItem(
        @StringRes override val linkResId: Int,
        val link: String
    ) : LinkItem()

    open class InternalLinkItem(@StringRes override val linkResId: Int) : LinkItem()
}

@Composable
fun IntroScreen(
    @StringRes titleResId: Int = 0,
    descriptionItems: List<DescriptionItem> = emptyList(),
    @StringRes positiveButtonResId: Int = 0,
    @StringRes negativeButtonResId: Int = 0,
    linkResIds: List<LinkItem> = emptyList(),
    onNavigationClick: () -> Unit = {},
    onClickPositiveButton: () -> Unit = {},
    onClickNegativeButton: () -> Unit = {},
    onClickLink: (LinkItem) -> Unit = {},
    illustration: @Composable () -> Unit
) {
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    NoAppBarScreenWrapper(
        navigationIconToken = IconTokens.actionCloseOutlined,
        onNavigationClick = onNavigationClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                val illustrationHeight = if (portrait) 180.dp else 90.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(illustrationHeight),
                    contentAlignment = Alignment.Center
                ) {
                    illustration()
                }
                val textAlign: TextAlign = if (portrait) TextAlign.Start else TextAlign.Center
                Text(
                    text = stringResource(id = titleResId),
                    style = DashlaneTheme.typography.titleSectionLarge,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    modifier = Modifier
                        .padding(bottom = 4.dp, top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = textAlign
                )
                DescriptionItems(descriptionItems)
                LinkButtons(linkResIds, onClickLink)
            }
            ButtonMediumBar(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 18.dp)
                    .align(Alignment.End)
                    .widthIn(max = 640.dp),
                primaryText = stringResource(id = positiveButtonResId),
                onPrimaryButtonClick = onClickPositiveButton,
                secondaryText = stringResource(id = negativeButtonResId),
                onSecondaryButtonClick = onClickNegativeButton
            )
        }
    }
}

@Composable
private fun DescriptionItems(descriptionItems: List<DescriptionItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        descriptionItems.forEach { item ->
            DescriptionItemContent(item)
        }
    }
}

@Composable
private fun LinkButtons(
    linkResIds: List<LinkItem>,
    onClickLink: (LinkItem) -> Unit
) {
    linkResIds.forEach { link ->
        val destinationType = if (link is LinkItem.ExternalLinkItem) {
            LinkButtonDestinationType.EXTERNAL
        } else {
            LinkButtonDestinationType.INTERNAL
        }
        LinkButton(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(id = link.linkResId),
            style = DashlaneTheme.typography.componentLinkStandard,
            destinationType = destinationType,
            onClick = { onClickLink.invoke(link) }
        )
    }
}

@Composable
private fun DescriptionItemContent(item: DescriptionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            token = item.imageIconToken,
            contentDescription = null,
            tint = DashlaneTheme.colors.textBrandStandard,
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .background(
                    color = DashlaneTheme.colors.containerExpressiveBrandQuietIdle,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(id = item.titleResId),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard
        )
    }
}

@Composable
@Preview
@Preview(heightDp = 360, widthDp = 800)
private fun PreviewIntroScreen() {
    DashlanePreview {
        IntroScreen(
            titleResId = R.string.notification_channel_vpn_title,
            descriptionItems = listOf(
                DescriptionItem(
                    imageIconToken = IconTokens.featureDarkWebMonitoringOutlined,
                    titleResId = R.string.generic_error_title
                ),
                DescriptionItem(
                    imageIconToken = IconTokens.notificationOutlined,
                    titleResId = R.string.generic_error_title
                ),
                DescriptionItem(
                    imageIconToken = IconTokens.lockOutlined,
                    titleResId = R.string.generic_error_title
                )
            ),
            linkResIds = listOf(
                LinkItem.ExternalLinkItem(R.string.generic_error_title, "http://dashlane.com"),
                LinkItem.ExternalLinkItem(R.string.generic_error_title, "http://dashlane.com")
            ),
            positiveButtonResId = R.string.ok,
            negativeButtonResId = R.string.cancel,
            onNavigationClick = {}
        ) {
            Image(
                painter = painterResource(id = R.drawable.device_android),
                contentDescription = stringResource(id = R.string.notification_channel_vpn_title)
            )
        }
    }
}