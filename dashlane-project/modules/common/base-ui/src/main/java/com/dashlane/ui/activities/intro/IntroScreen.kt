package com.dashlane.ui.activities.intro

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R
import com.dashlane.ui.common.compose.components.basescreen.NoAppBarScreenWrapper
import com.dashlane.ui.common.compose.components.DescriptionItemContent

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
@Suppress("LongMethod")
fun IntroScreen(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int = 0,
    @StringRes titleHeader: Int? = null,
    descriptionItems: List<DescriptionItem> = emptyList(),
    @StringRes positiveButtonResId: Int = 0,
    @StringRes negativeButtonResId: Int = 0,
    linkItems: List<LinkItem> = emptyList(),
    onNavigationClick: () -> Unit = {},
    onClickPositiveButton: () -> Unit = {},
    onClickNegativeButton: () -> Unit = {},
    onClickLink: (LinkItem) -> Unit = {},
    illustration: @Composable () -> Unit
) {
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    NoAppBarScreenWrapper(
        modifier = Modifier.background(color = DashlaneTheme.colors.backgroundDefault),
        navigationIconToken = IconTokens.actionCloseOutlined,
        onNavigationClick = onNavigationClick
    ) {
        Column(
            modifier = modifier
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
                Spacer(modifier = Modifier.size(24.dp))
                titleHeader?.let {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        text = stringResource(id = it),
                        style = DashlaneTheme.typography.bodyStandardRegular,
                        color = DashlaneTheme.colors.textNeutralQuiet
                    )
                }
                val textAlign: TextAlign = if (portrait) TextAlign.Start else TextAlign.Center
                Text(
                    text = stringResource(id = titleResId),
                    style = DashlaneTheme.typography.titleSectionLarge,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .fillMaxWidth(),
                    textAlign = textAlign
                )
                DescriptionItems(
                    modifier = Modifier.padding(vertical = 24.dp),
                    descriptionItems = descriptionItems
                )
                LinkButtons(linkItems, onClickLink)
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
private fun DescriptionItems(
    modifier: Modifier = Modifier,
    descriptionItems: List<DescriptionItem>
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        descriptionItems.forEach { item ->
            DescriptionItemContent(
                iconToken = item.imageIconToken,
                title = stringResource(id = item.titleResId)
            )
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
@Preview
@Preview(device = Devices.TABLET)
private fun PreviewIntroScreen() {
    DashlanePreview {
        IntroScreen(
            titleResId = R.string.notification_channel_vpn_title,
            titleHeader = R.string.generic_error_title,
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
            linkItems = listOf(
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