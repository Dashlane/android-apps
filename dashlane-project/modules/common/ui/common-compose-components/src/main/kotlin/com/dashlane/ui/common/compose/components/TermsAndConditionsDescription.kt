package com.dashlane.ui.common.compose.components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dashlane.Legal
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R

@Composable
fun ColumnScope.TermsAndConditionsDescription(
    offersCheck: Boolean,
    onOffersCheckChange: (Boolean) -> Unit,
    tosCheck: Boolean,
    onTosCheckChange: (Boolean) -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit,
) {
    Text(
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        text = stringResource(id = R.string.passwordless_terms_and_conditions_description),
        style = DashlaneTheme.typography.bodyStandardRegular,
    )
    CheckboxWithLabel(
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(id = R.string.passwordless_terms_and_conditions_privacy_policy),
        checked = offersCheck,
    ) {
        onOffersCheckChange(!offersCheck)
    }
    Spacer(modifier = Modifier.height(8.dp))
    CheckboxWithLabel(
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(id = R.string.passwordless_terms_and_conditions_terms_of_services),
        checked = tosCheck,
    ) {
        onTosCheckChange(!tosCheck)
    }
    Spacer(modifier = Modifier.height(8.dp))
    LinkButton(
        modifier = Modifier.padding(start = 40.dp),
        onClick = { onOpenHelpCenterPage(Legal.URL_TERMS_OF_SERVICE.toUri()) },
        text = stringResource(R.string.passwordless_terms_and_conditions_terms_of_services_button),
        destinationType = LinkButtonDestinationType.EXTERNAL,
    )
    LinkButton(
        modifier = Modifier.padding(start = 40.dp),
        onClick = { onOpenHelpCenterPage(Legal.URL_PRIVACY_POLICY.toUri()) },
        text = stringResource(R.string.passwordless_terms_and_conditions_privacy_policy_button),
        destinationType = LinkButtonDestinationType.EXTERNAL,
    )
}

@Preview
@Composable
private fun TermsAndConditionsDescriptionPreview() {
    DashlanePreview {
        Column {
            TermsAndConditionsDescription(
                offersCheck = true,
                onOffersCheckChange = {},
                tosCheck = true,
                onTosCheckChange = {},
                onOpenHelpCenterPage = {},
            )
        }
    }
}