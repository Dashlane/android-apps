package com.dashlane.createaccount.passwordless.termsandconditions

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dashlane.Legal
import com.dashlane.R
import com.dashlane.createaccount.passwordless.MplessAccountCreationViewModel
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.DashlaneCheckbox

@Composable
fun TermsAndConditionsScreen(
    modifier: Modifier = Modifier,
    viewModel: MplessAccountCreationViewModel,
    onCreateAccount: () -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit
) {
    val userDataState by viewModel.userDataStateFlow.collectAsState()

    TermAndConditionsContent(
        modifier = modifier,
        tosCheck = userDataState.termsOfServicesAccepted,
        privacyPolityCheck = userDataState.privacyPolicyAccepted,
        onCreateAccountClick = onCreateAccount,
        onTosCheckChange = { viewModel.updateTos(it) },
        onPrivacyPolicyChange = { viewModel.updatePrivacyPolicy(it) },
        onOpenHelpCenterPage = onOpenHelpCenterPage
    )
}

@Suppress("LongMethod")
@Composable
fun TermAndConditionsContent(
    modifier: Modifier = Modifier,
    tosCheck: Boolean,
    privacyPolityCheck: Boolean,
    onCreateAccountClick: () -> Unit,
    onTosCheckChange: (Boolean) -> Unit,
    onPrivacyPolicyChange: (Boolean) -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(id = R.string.passwordless_terms_and_conditions_title),
            style = DashlaneTheme.typography.titleSectionLarge
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = R.string.passwordless_terms_and_conditions_description),
            style = DashlaneTheme.typography.bodyStandardRegular
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashlaneCheckbox(
                checked = privacyPolityCheck,
                onCheckedChange = onPrivacyPolicyChange
            )
            Text(
                text = stringResource(id = R.string.passwordless_terms_and_conditions_privacy_policy),
                style = DashlaneTheme.typography.bodyStandardRegular
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashlaneCheckbox(
                checked = tosCheck,
                onCheckedChange = onTosCheckChange,
            )
            Text(
                text = stringResource(id = R.string.passwordless_terms_and_conditions_terms_of_services),
                style = DashlaneTheme.typography.bodyStandardRegular
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LinkButton(
            modifier = Modifier.padding(start = 48.dp),
            onClick = { onOpenHelpCenterPage(Legal.URL_TERMS_OF_SERVICE.toUri()) },
            text = stringResource(R.string.passwordless_terms_and_conditions_terms_of_services_button),
            destinationType = LinkButtonDestinationType.EXTERNAL
        )
        LinkButton(
            modifier = Modifier.padding(start = 48.dp),
            onClick = { onOpenHelpCenterPage(Legal.URL_PRIVACY_POLICY.toUri()) },
            text = stringResource(R.string.passwordless_terms_and_conditions_privacy_policy_button),
            destinationType = LinkButtonDestinationType.EXTERNAL
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            enabled = tosCheck,
            onClick = onCreateAccountClick,
            mood = Mood.Brand,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.passwordless_account_creation_create_account_button)
            )
        )
    }
}

@Preview
@Composable
private fun TermsAndConditionsScreenPreview() {
    DashlanePreview {
        TermAndConditionsContent(
            modifier = Modifier,
            tosCheck = true,
            privacyPolityCheck = false,
            onCreateAccountClick = {},
            onTosCheckChange = {},
            onPrivacyPolicyChange = {},
            onOpenHelpCenterPage = {}
        )
    }
}
