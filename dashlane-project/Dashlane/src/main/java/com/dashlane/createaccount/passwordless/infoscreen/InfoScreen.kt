package com.dashlane.createaccount.passwordless.infoscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.dashlane.design.component.InfoboxMedium
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

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
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(24.dp)
        ) {
            Text(
                modifier = modifier.padding(top = 24.dp),
                text = stringResource(id = R.string.passwordless_info_screen_title),
                style = DashlaneTheme.typography.titleSectionLarge
            )
            Text(
                modifier = modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.passwordless_info_screen_description),
                style = DashlaneTheme.typography.bodyStandardRegular
            )
            InfoboxMedium(
                modifier = modifier.padding(top = 32.dp),
                title = stringResource(id = R.string.passwordless_info_screen_info_card_content),
                mood = Mood.Neutral
            )
        }
        ButtonMediumBar(
            modifier = modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
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