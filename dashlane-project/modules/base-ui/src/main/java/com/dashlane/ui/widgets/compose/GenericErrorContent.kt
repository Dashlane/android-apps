package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

@Composable
fun GenericErrorContent(
    modifier: Modifier = Modifier,
    title: String? = stringResource(id = R.string.generic_error_title),
    message: String? = stringResource(id = R.string.generic_error_message),
    textPrimary: String,
    textSecondary: String,
    onClickPrimary: () -> Unit,
    onClickSecondary: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.ic_error_state),
            colorFilter = ColorFilter.tint(DashlaneTheme.colors.textDangerQuiet.value),
            contentDescription = ""
        )
        title?.let {
            Text(
                text = title,
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
        message?.let {
            Text(
                text = message,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ButtonMediumBar(
            primaryText = textPrimary,
            secondaryText = textSecondary,
            onPrimaryButtonClick = onClickPrimary,
            onSecondaryButtonClick = onClickSecondary
        )
    }
}

@Preview
@Composable
fun GenericErrorContentPreview() {
    DashlanePreview {
        GenericErrorContent(
            title = "Title",
            message = "Message",
            textPrimary = "Primary",
            textSecondary = "Secondary",
            onClickPrimary = {},
            onClickSecondary = {}
        )
    }
}
