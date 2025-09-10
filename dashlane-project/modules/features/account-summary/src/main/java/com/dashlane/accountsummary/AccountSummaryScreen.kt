package com.dashlane.accountsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Card
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.SnackbarType
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun AccountSummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountSummaryViewModel,
    goToLock: () -> Unit,
    goToChangeContactEmail: () -> Unit,
    goToChangeLoginEmail: () -> Unit,
) {
    val snackbarState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel) {
        viewModel.loadEmails()
        viewModel.uiState.sideEffect.collect { navState ->
            when (navState) {
                is AccountSummaryNavState.EditContactForm -> goToChangeContactEmail()
                is AccountSummaryNavState.EditLoginForm -> goToChangeLoginEmail()
                AccountSummaryNavState.LockPrompt -> goToLock()
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState.isError) {
            snackbarState.showSnackbar(context.getString(R.string.network_failed_notification))
        }
    }

    Box {
        AccountSummaryContent(
            modifier = modifier,
            loginEmail = uiState.loginEmail,
            contactEmail = uiState.contactEmail,
            canChangeLoginEmail = uiState.canChangeLoginEmail,
            onClickEditLoginEmail = viewModel::onClickEditLoginEmail,
            onClickEditContactEmail = viewModel::onClickEditContactEmail,
        )
        DashlaneSnackbarWrapper(type = SnackbarType.ERROR) {
            SnackbarHost(modifier = Modifier.align(Alignment.BottomCenter), hostState = snackbarState)
        }
    }
}

@Composable
fun AccountSummaryContent(
    modifier: Modifier = Modifier,
    loginEmail: String?,
    contactEmail: String?,
    canChangeLoginEmail: Boolean,
    onClickEditLoginEmail: () -> Unit,
    onClickEditContactEmail: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LoginDetailsFrame(loginEmail, canChangeLoginEmail, onClickEditLoginEmail)
        AccountVerificationFrame(contactEmail, onClickEditContactEmail)
    }
}

@Composable
fun LoginDetailsFrame(
    loginEmail: String?,
    canChangeLoginEmail: Boolean,
    onClickEditLoginEmail: () -> Unit,
) {
    Card {
        Column {
            Text(
                text = stringResource(id = R.string.account_status_login_details),
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (canChangeLoginEmail) {
                    stringResource(id = R.string.account_summary_login_details_email_edit)
                } else {
                    stringResource(id = R.string.account_status_login_details_email)
                },
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
            )
            if (!canChangeLoginEmail) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.account_status_login_details_help),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            DisplayField(
                value = loginEmail ?: "",
                label = stringResource(id = R.string.account_status_login_details_email_field),
                actions = if (!canChangeLoginEmail) {
                    null
                } else {
                    DisplayFieldActions.newInstance(
                        FieldAction.Generic(
                            iconLayout = ButtonLayout.IconOnly(
                                iconToken = IconTokens.actionEditOutlined,
                                contentDescription = stringResource(
                                    id = R.string.account_status_login_email_edit_accessibility_description,
                                ),
                            ),
                        ) {
                            onClickEditLoginEmail()
                            true
                        },
                    )
                },
            )
        }
    }
}

@Composable
fun AccountVerificationFrame(
    contactEmail: String?,
    onClickEditContactEmail: () -> Unit,
) {
    Card {
        Column {
            Text(
                text = stringResource(id = R.string.account_status_account_verification),
                style = DashlaneTheme.typography.titleBlockMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(id = R.string.account_status_account_verification_details),
                style = DashlaneTheme.typography.bodyReducedRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
            )
            Spacer(modifier = Modifier.height(16.dp))
            DisplayField(
                label = stringResource(id = R.string.account_status_account_verification_contact_field),
                value = contactEmail ?: "",
                actions = DisplayFieldActions.newInstance(
                    FieldAction.Generic(
                        iconLayout = ButtonLayout.IconOnly(
                            iconToken = IconTokens.actionEditOutlined,
                            contentDescription = stringResource(id = R.string.account_status_account_verification_edit_description),
                        ),
                    ) {
                        onClickEditContactEmail()
                        true
                    },
                ),
            )
        }
    }
}

@Preview
@Composable
private fun AccountVerificationPreview() {
    DashlanePreview {
        AccountVerificationFrame(
            contactEmail = "randomemail@provider.com",
            onClickEditContactEmail = {},
        )
    }
}

@Preview
@Composable
private fun LoginDetailsPreview() {
    DashlanePreview {
        LoginDetailsFrame(
            loginEmail = "randomemail@provider.com",
            canChangeLoginEmail = true,
            onClickEditLoginEmail = {},
        )
    }
}

@Preview
@Composable
private fun AccountStatusDisplayFormPreview() {
    DashlanePreview {
        AccountSummaryContent(
            loginEmail = "randomemail@provider.com",
            contactEmail = "randomemail@provider.com",
            canChangeLoginEmail = true,
            onClickEditLoginEmail = {},
            onClickEditContactEmail = {},
        )
    }
}