package com.dashlane.autofill.phishing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.api.R
import com.dashlane.autofill.phishing.PhishingWarningState
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.DisplayFieldMood
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.IndeterminateLoading

@Composable
internal fun PhishingWarningDialog(
    state: PhishingWarningState.ViewState,
    onTrustWebsite: () -> Unit,
    onMainActionClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (state) {
        PhishingWarningState.ViewState.Initial -> {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                IndeterminateLoading(modifier = Modifier.size(56.dp))
            }
        }
        is PhishingWarningState.ViewState.Loaded -> {
            Dialog(
                title = stringResource(id = R.string.phishing_warning_title_autofill),
                description = {
                    DialogBody(
                        itemWebsite = state.itemWebsite,
                        suspiciousWebsite = state.suspiciousWebsite,
                    )
                },
                additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.phishing_warning_trust)),
                additionalActionClick = onTrustWebsite,
                mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.phishing_warning_not_trust)),
                mainActionClick = onMainActionClick,
                onDismissRequest = onDismissRequest,
            )
        }
    }
}

@Composable
fun DialogBody(
    itemWebsite: String,
    suspiciousWebsite: String,
) {
    Column {
        Text(text = stringResource(R.string.phishing_warning_content))
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .background(DashlaneTheme.colors.containerAgnosticNeutralQuiet)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = DashlaneTheme.colors.borderNeutralQuietIdle,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            DisplayField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.phishing_warning_trusted_url),
                value = itemWebsite,
                leadingIcon = IconTokens.feedbackSuccessOutlined,
                mood = DisplayFieldMood.Positive(),
            )
            DisplayField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.phishing_warning_current_url),
                value = suspiciousWebsite,
                leadingIcon = IconTokens.feedbackWarningOutlined,
                mood = DisplayFieldMood.Warning(),
            )
        }
    }
}

@Preview
@Composable
private fun PhishingDialogPreview() {
    DashlanePreview {
        PhishingWarningDialog(
            state = PhishingWarningState.ViewState.Loaded(
                suspiciousWebsite = "facebook.com",
                itemWebsite = "dashlane.com",
            ),
            onTrustWebsite = {},
            onMainActionClick = {},
            onDismissRequest = {},
        )
    }
}