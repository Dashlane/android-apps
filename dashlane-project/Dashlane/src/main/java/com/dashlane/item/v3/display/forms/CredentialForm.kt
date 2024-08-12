package com.dashlane.item.v3.display.forms

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.authenticator.Hotp
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.PasswordHealthData
import com.dashlane.item.v3.display.fields.DeleteLoginField
import com.dashlane.item.v3.display.sections.HealthDetailSection
import com.dashlane.item.v3.display.sections.LoginDetailSection
import com.dashlane.item.v3.display.sections.OrganisationSection
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.State
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField

@Suppress("LongMethod")
fun LazyListScope.credentialForm(
    viewModel: CredentialItemEditViewModel,
    uiState: State,
    data: CredentialFormData
) {
    credentialFormContent(
        uiState = uiState,
        data = data,
        passwordActions = PasswordActions(
            onPasswordUpdate = {
                viewModel.updatePassword(it)
            },
            onShowPassword = {
                viewModel.showSensitiveField(SensitiveField.PASSWORD)
            },
            onHidePassword = {
                viewModel.hideSensitiveField(SensitiveField.PASSWORD)
            },
            onCopyPassword = {
                viewModel.copyToClipboard(CopyField.Password)
            },
            onGeneratePassword = {
                viewModel.actionGeneratePassword()
            },
            onLimitedRightInfoOpen = {
                viewModel.actionOpenNoRights()
            }
        ),
        loginActions = LoginActions(
            onValueChanged = { value ->
                viewModel.updateFormDataFromView(value)
            },
            onValueCopy = {
                viewModel.copyToClipboard(it)
            },
            onLinkedServicesOpen = {
                viewModel.actionOpenLinkedServices(addNew = false)
            },
            onWebsiteOpen = {
                viewModel.actionOpenWebsite()
            },
            onSetupTwoFactorAuthenticator = {
                viewModel.actionSetupTwoFactor()
            },
            onRemoveTwoFactorAuthenticator = {
                viewModel.actionRemoveTwoFactor()
            },
            onHotpRefreshed = {
                viewModel.refreshHotp(it)
            }
        ),
        organisationActions = OrganisationActions(
            onValueChanged = { value ->
                viewModel.updateFormDataFromView(value)
            },
            onCollectionOpen = {
                viewModel.actionOpenCollection(data.collections, data.space?.teamId, data.isSharedWithLimitedRight)
            },
            onCollectionRemove = {
                viewModel.removeCollection(it)
            },
            onSharedClick = {
                viewModel.actionOpenShared()
            }
        ),
        onDeleteAction = {
            viewModel.actionDelete()
        }
    )
}

private fun LazyListScope.credentialFormContent(
    uiState: State,
    data: CredentialFormData,
    passwordActions: PasswordActions,
    loginActions: LoginActions,
    organisationActions: OrganisationActions,
    onDeleteAction: () -> Unit
) {
    item {
        LoginDetailSection(
            data = data,
            isNewItem = uiState.isNew,
            revealedFields = uiState.revealedFields,
            editMode = uiState.isEditMode,
            passwordActions = passwordActions,
            loginActions = loginActions
        )
    }
    item {
        HealthDetailSection(data = data, editMode = uiState.isEditMode)
    }
    item {
        OrganisationSection(data = data, editMode = uiState.isEditMode, organisationActions = organisationActions)
    }
    item {
        DeleteLoginField(
            canDelete = uiState.formData.canDelete,
            editMode = uiState.isEditMode,
            onDeleteAction = onDeleteAction
        )
    }
}

@Composable
@Preview
private fun CredentialFormContentPreview() {
    DashlanePreview {
        LazyColumn(
            content = {
                credentialFormContent(
                    uiState = State.emptyState("", false),
                    data = credentialFormDemoData(),
                    passwordActions = PasswordActions(),
                    loginActions = LoginActions(),
                    organisationActions = OrganisationActions(),
                    onDeleteAction = {}
                )
            }
        )
    }
}

@Composable
@Preview
private fun CredentialFormContentPreviewEdit() {
    DashlanePreview {
        LazyColumn(
            content = {
                credentialFormContent(
                    uiState = State.emptyState("", true),
                    data = CredentialFormData(),
                    passwordActions = PasswordActions(),
                    loginActions = LoginActions(),
                    organisationActions = OrganisationActions(),
                    onDeleteAction = {}
                )
            }
        )
    }
}

private fun credentialFormDemoData() = CredentialFormData(
    email = "randomemail@provider.com",
    secondaryLogin = "secondaryLogin",
    url = "https://www.dashlane.com",
    space = TeamSpace.Personal,
    passwordHealth = PasswordHealthData(
        passwordStrength = PasswordStrength(PasswordStrengthScore.SAFELY_UNGUESSABLE, null, listOf()),
        isCompromised = false,
        reusedCount = 0,
        isPasswordEmpty = false
    ),
)

data class OrganisationActions(
    val onValueChanged: (CredentialFormData) -> Unit = {},
    val onCollectionOpen: () -> Unit = {},
    val onCollectionRemove: (CollectionData) -> Unit = {},
    val onSharedClick: () -> Unit = {}
)

data class LoginActions(
    val onValueChanged: (CredentialFormData) -> Unit = {},
    val onValueCopy: (CopyField) -> Unit = {},
    val onLinkedServicesOpen: () -> Unit = {},
    val onWebsiteOpen: () -> Unit = {},
    val onSetupTwoFactorAuthenticator: () -> Unit = {},
    val onRemoveTwoFactorAuthenticator: () -> Unit = {},
    val onHotpRefreshed: (Hotp) -> Unit = {}
)

data class PasswordActions(
    val onPasswordUpdate: (String) -> Unit = {},
    val onShowPassword: () -> Unit = {},
    val onHidePassword: () -> Unit = {},
    val onCopyPassword: () -> Unit = {},
    val onGeneratePassword: () -> Unit = {},
    val onLimitedRightInfoOpen: () -> Unit = {},
)