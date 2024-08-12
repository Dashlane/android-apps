package com.dashlane.authenticator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.ContentStepper
import com.dashlane.ui.common.compose.components.basescreen.NoAppBarScreenWrapper

@Composable
fun AuthenticatorSunsetInfoScreen(
    modifier: Modifier = Modifier,
    onLearnMoreClick: () -> Unit,
    onSettingClick: () -> Unit,
    onNavigationClick: () -> Unit
) {
    NoAppBarScreenWrapper(
        navigationIconToken = IconTokens.arrowLeftOutlined,
        onNavigationClick = onNavigationClick
    ) {
        AuthenticatorSunsetInfoContent(
            modifier = modifier,
            onLearnMoreClick = onLearnMoreClick,
            onSettingClick = onSettingClick
        )
    }
}

@Composable
fun AuthenticatorSunsetInfoContent(
    modifier: Modifier,
    onLearnMoreClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.sunset_info_title),
            style = DashlaneTheme.typography.titleSectionLarge
        )
        HtmlText(
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            htmlText = stringResource(R.string.sunset_info_description),
            style = DashlaneTheme.typography.bodyStandardRegular
        )
        ContentStepper(
            content = listOf(
                stringResource(id = R.string.sunset_info_step1),
                stringResource(id = R.string.sunset_info_step2),
                stringResource(id = R.string.sunset_info_step3),
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            modifier = modifier
                .align(Alignment.End),
            primaryText = stringResource(id = R.string.sunset_info_cta_setting),
            secondaryText = stringResource(id = R.string.sunset_info_cta_learn_more),
            onPrimaryButtonClick = onSettingClick,
            onSecondaryButtonClick = onLearnMoreClick
        )
    }
}

@Preview
@Composable
private fun InfoScreenPreview() {
    DashlanePreview {
        AuthenticatorSunsetInfoScreen(
            modifier = Modifier,
            onSettingClick = { },
            onLearnMoreClick = { },
            onNavigationClick = { }
        )
    }
}