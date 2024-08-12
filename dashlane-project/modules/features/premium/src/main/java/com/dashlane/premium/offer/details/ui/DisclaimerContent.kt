package com.dashlane.premium.offer.details.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.premium.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DisclaimerContent(
    modifier: Modifier = Modifier,
    onClickTosLink: () -> Unit,
    onClickPrivacyLink: () -> Unit,
    isVisible: Boolean
) {
    if (!isVisible) return

    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp),
        horizontalAlignment = if (portrait) Alignment.Start else Alignment.End
    ) {
        Text(
            style = DashlaneTheme.typography.bodyReducedRegular,
            text = if (portrait) {
                stringResource(id = R.string.plan_disclaimer_default_v2)
            } else {
                stringResource(id = R.string.plan_disclaimer_default_v2_long)
            },
            textAlign = if (portrait) {
                TextAlign.Start
            } else {
                TextAlign.End
            }
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            maxItemsInEachRow = 2
        ) {
            LinkButton(
                text = stringResource(id = R.string.plan_disclaimer_legal_arg_privacy_policy),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onClickPrivacyLink
            )
            LinkButton(
                text = stringResource(id = R.string.plan_disclaimer_legal_arg_terms_of_service),
                destinationType = LinkButtonDestinationType.EXTERNAL,
                onClick = onClickTosLink
            )
        }
    }
}

@Preview
@Composable
private fun DisclaimerContentPreview() = DashlanePreview {
    DisclaimerContent(
        onClickTosLink = { },
        onClickPrivacyLink = { },
        isVisible = true
    )
}