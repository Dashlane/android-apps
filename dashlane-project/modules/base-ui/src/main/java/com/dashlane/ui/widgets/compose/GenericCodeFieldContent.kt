package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme

@Composable
fun GenericCodeInputContent(
    modifier: Modifier = Modifier,
    text: String,
    login: String,
    isLoading: Boolean,
    errorMessage: String?,
    isTokenError: Boolean,
    onOtpComplete: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashlaneLogo()
        Text(
            text = login,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = text,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 48.dp)
        )
        OtpInput(
            modifier = Modifier.padding(top = 16.dp),
            onOtpComplete = onOtpComplete,
            isError = isTokenError,
            error = if (isTokenError) errorMessage else null
        )
        if (!isTokenError && errorMessage != null) {
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = errorMessage,
                color = DashlaneTheme.colors.textDangerQuiet,
                style = DashlaneTheme.typography.bodyHelperRegular
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun GenericCodeInputContentPreview() {
    DashlaneTheme(darkTheme = true) {
        OtpInput(onOtpComplete = { }, isError = true, error = "Error")
    }
}