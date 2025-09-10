package com.dashlane.accountsummary.common

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.accountsummary.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.EmailTextField


@SuppressWarnings("kotlin:S107")
@Composable
fun ChangeEmailContent(
    modifier: Modifier = Modifier,
    title: String,
    currentEmailLabel: String,
    newEmailLabel: String,
    currentEmail: String?,
    newEmail: TextFieldValue?,
    saveEnabled: Boolean,
    error: String? = null,
    onNewEmailChange: (TextFieldValue) -> Unit,
    onSendNewEmailClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {},
            label = currentEmailLabel,
            value = currentEmail ?: "",
            readOnly = true,
        )
        EmailTextField(
            modifier = Modifier.fillMaxWidth(),
            label = newEmailLabel,
            email = newEmail ?: TextFieldValue(""),
            onEmailChange = onNewEmailChange,
            error = error,
            onClickContinue = onSendNewEmailClicked,
        )
        ButtonMedium(
            modifier = Modifier.align(alignment = Alignment.End),
            onClick = onSendNewEmailClicked,
            layout = ButtonLayout.TextOnly(stringResource(id = R.string.account_status_change_contact_email_confirm)),
            enabled = saveEnabled,
        )
    }
}

@Preview
@Composable
private fun ChangeContactEmailScreenPreview() {
    DashlanePreview {
        ChangeEmailContent(
            title = stringResource(id = R.string.account_status_change_contact_email),
            currentEmailLabel = stringResource(id = R.string.account_status_change_contact_email_current),
            newEmailLabel = stringResource(id = R.string.account_status_change_contact_email_new),
            currentEmail = "randomemail@provider.com",
            newEmail = TextFieldValue("randomemail@provider.com"),
            onNewEmailChange = {},
            onSendNewEmailClicked = {},
            saveEnabled = true,
        )
    }
}

@Preview
@Composable
private fun ChangeLoinEmailScreenPreview() {
    DashlanePreview {
        ChangeEmailContent(
            title = stringResource(id = R.string.change_login_email_title),
            currentEmailLabel = stringResource(id = R.string.change_login_email_previous_field_label),
            newEmailLabel = stringResource(id = R.string.change_login_email_new_field_label),
            currentEmail = "randomemail@provider.com",
            newEmail = TextFieldValue("randomemail@provider.com"),
            onNewEmailChange = {},
            onSendNewEmailClicked = {},
            saveEnabled = true,
        )
    }
}