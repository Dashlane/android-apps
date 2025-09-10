package com.dashlane.authenticator.suggestions.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.authenticator.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun AuthenticatorNo2FACompatibleLogins(
    onAddLoginClick: () -> Unit,
    onAdd2faTokenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpressiveIcon(
            icon = IconTokens.featureAuthenticatorOutlined,
            size = ExpressiveIconSize.Large,
            mood = Mood.Brand,
        )
        Spacer(
            modifier = Modifier.height(24.dp),
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
            text = stringResource(R.string.authenticator_empty_screen_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
            textAlign = TextAlign.Center,
        )
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(R.string.authenticator_empty_screen_body),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
        )
        Spacer(
            modifier = Modifier.height(10.dp),
        )
        ButtonMedium(
            layout = ButtonLayout.TextOnly(text = stringResource(R.string.authenticator_empty_screen_add_button)),
            onClick = onAddLoginClick,
            mood = Mood.Brand,
            intensity = Intensity.Catchy,
        )
        ButtonMedium(
            layout = ButtonLayout.TextOnly(text = stringResource(R.string.authenticator_empty_screen_setup_button)),
            onClick = onAdd2faTokenClick,
            mood = Mood.Brand,
            intensity = Intensity.Quiet,
        )
        Spacer(
            modifier = Modifier.height(2.dp),
        )
    }
}

@Preview
@Composable
private fun AuthenticatorNo2FACompatibleLoginsPreview() {
    DashlanePreview {
        AuthenticatorNo2FACompatibleLogins(
            onAddLoginClick = {},
            onAdd2faTokenClick = {},
        )
    }
}