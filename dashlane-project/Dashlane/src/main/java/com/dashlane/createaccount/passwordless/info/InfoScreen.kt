package com.dashlane.createaccount.passwordless.info

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
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.ContentStepper

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    onLearnMoreClick: () -> Unit,
    onNextClick: () -> Unit
) {
    InfoContent(
        modifier = modifier,
        onLearnMoreClick = onLearnMoreClick,
        onNextClick = onNextClick
    )
}

@Composable
fun InfoContent(
    modifier: Modifier,
    onLearnMoreClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 48.dp, bottom = 24.dp),
            text = stringResource(id = R.string.passwordless_info_screen_title),
            style = DashlaneTheme.typography.titleSectionLarge
        )
        ContentStepper(
            content = listOf(
                stringResource(id = R.string.passwordless_info_screen_content_step1),
                stringResource(R.string.passwordless_info_screen_content_step2),
                stringResource(id = R.string.passwordless_info_screen_content_step3),
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMediumBar(
            modifier = modifier
                .align(Alignment.End),
            primaryText = stringResource(id = R.string.passwordless_info_screen_button_get_started),
            secondaryText = stringResource(id = R.string.passwordless_info_screen_button_learn_more),
            onPrimaryButtonClick = onNextClick,
            onSecondaryButtonClick = onLearnMoreClick
        )
    }
}

@Preview
@Composable
fun InfoScreenPreview() {
    DashlanePreview {
        InfoContent(
            modifier = Modifier,
            onNextClick = { },
            onLearnMoreClick = { }
        )
    }
}