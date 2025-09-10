package com.dashlane.csvexport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.help.HelpCenterLink
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.IntroScreen
import com.dashlane.ui.activities.intro.LinkItem

@Composable
fun CSVExportIntroScreen(
    onClose: () -> Unit,
    onClickPositiveButton: () -> Unit,
    onClickExternalLink: (String) -> Unit,
) {
    IntroScreen(
        titleResId = R.string.csv_export_intro_title,
        descriptionItems = listOf(
            DescriptionItem(
                imageIconToken = null,
                titleResId = R.string.csv_export_intro_description,
            ),
        ),
        linkItems = listOf(
            LinkItem.ExternalLinkItem(
                linkResId = R.string.csv_export_on_web_button_export_help_center,
                link = HelpCenterLink.ARTICLE_CSV_EXPORT_WEB.uri.toString(),
            ),
        ),
        onClickLink = {
            if (it is LinkItem.ExternalLinkItem) {
                onClickExternalLink(it.link)
            }
        },
        positiveButtonResId = R.string.csv_export_intro_positive_button_text,
        negativeButtonResId = R.string.cancel,
        onNavigationClick = onClose,
        onClickNegativeButton = onClose,
        onClickPositiveButton = onClickPositiveButton,
        illustration = {
            Box(modifier = Modifier.fillMaxSize()) {
                ExpressiveIcon(
                    modifier = Modifier.align(Alignment.BottomStart),
                    icon = IconTokens.csvOutlined,
                    size = ExpressiveIconSize.Large,
                )
            }
        },
    )
}

@Preview
@Composable
private fun CSVExportIntroScreenPreview() = DashlanePreview {
    CSVExportIntroScreen({}, {}, {})
}