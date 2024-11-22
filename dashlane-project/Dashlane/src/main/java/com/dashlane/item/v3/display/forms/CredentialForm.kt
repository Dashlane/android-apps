package com.dashlane.item.v3.display.forms

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.authenticator.Hotp
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.PasswordHealthData
import com.dashlane.item.v3.display.fields.DeleteItemField
import com.dashlane.item.v3.display.sections.AttachmentsSection
import com.dashlane.item.v3.display.sections.HealthDetailSection
import com.dashlane.item.v3.display.sections.LoginDetailSection
import com.dashlane.item.v3.display.sections.OrganisationSection
import com.dashlane.item.v3.display.sharedAccessSectionItem
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.Datas
import com.dashlane.item.v3.viewmodels.FormState
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.xml.domain.SyncObject

@Suppress("LongMethod")
fun LazyListScope.credentialForm(
    viewModel: CredentialItemEditViewModel,
    uiState: FormState<CredentialFormData>
) {
    credentialFormContent(
        uiState = uiState,
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
                viewModel.updateCurrentDataFromView(value)
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
                viewModel.updateCommonDataFromView(value)
            },
            onCollectionOpen = {
                viewModel.actionOpenCollection()
            },
            onCollectionRemove = {
                viewModel.removeCollection(it)
            }
        ),
        onSharedClick = {
            viewModel.actionOpenShared()
        },
        onDeleteAction = {
            viewModel.actionDelete()
        },
        onViewAttachments = viewModel::onViewAttachments
    )
}

private fun LazyListScope.credentialFormContent(
    uiState: FormState<CredentialFormData>,
    passwordActions: PasswordActions,
    loginActions: LoginActions,
    organisationActions: OrganisationActions,
    onSharedClick: () -> Unit,
    onDeleteAction: () -> Unit,
    onViewAttachments: () -> Unit
) {
    item(key = "LoginDetailSection") {
        LoginDetailSection(
            data = uiState.datas.current,
            isNewItem = uiState.isNew,
            revealedFields = uiState.revealedFields,
            editMode = uiState.isEditMode,
            passwordActions = passwordActions,
            loginActions = loginActions
        )
    }
    item(key = "HealthDetailSection") {
        HealthDetailSection(data = uiState.datas.current, editMode = uiState.isEditMode)
    }
    item(key = "OrganisationSection") {
        OrganisationSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            organisationActions = organisationActions
        )
    }
    item(key = "AttachmentsSection") {
        AttachmentsSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            onViewAttachments = onViewAttachments
        )
    }
    sharedAccessSectionItem(uiState, onSharedClick)
    item(key = "DeleteLoginField") {
        DeleteItemField(
            canDelete = uiState.datas.current.commonData.canDelete,
            editMode = uiState.isEditMode,
            label = stringResource(id = R.string.vault_delete_login),
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
                val formData = credentialFormDemoData()
                credentialFormContent(
                    uiState = FormState(
                        "",
                        datas = Datas(current = formData, initial = formData),
                        isNew = false,
                        isEditMode = false
                    ),
                    passwordActions = PasswordActions(),
                    loginActions = LoginActions(),
                    organisationActions = OrganisationActions(),
                    onDeleteAction = {},
                    onViewAttachments = {},
                    onSharedClick = {}
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
                    uiState = FormState(
                        itemId = "",
                        datas = Datas(
                            current = Data(formData = CredentialFormData(), commonData = CommonData()),
                            initial = Data(formData = CredentialFormData(), commonData = CommonData())
                        ),
                        isEditMode = true,
                        isNew = false
                    ),
                    passwordActions = PasswordActions(),
                    loginActions = LoginActions(),
                    organisationActions = OrganisationActions(),
                    onDeleteAction = {},
                    onViewAttachments = {},
                    onSharedClick = {}
                )
            }
        )
    }
}

private fun credentialFormDemoData() = Data(
    formData = CredentialFormData(
        email = "randomemail@provider.com",
        secondaryLogin = "secondaryLogin",
        url = "https://www.dashlane.com",
        passwordHealth = PasswordHealthData(
            passwordStrength = PasswordStrength(PasswordStrengthScore.SAFELY_UNGUESSABLE, null, listOf()),
            isCompromised = false,
            reusedCount = 0,
            isPasswordEmpty = false
        ),
    ),
    commonData = CommonData(
        space = TeamSpace.Personal
    )
)

data class OrganisationActions(
    val onValueChanged: (CommonData) -> Unit = {},
    val onCollectionOpen: () -> Unit = {},
    val onCollectionRemove: (CollectionData) -> Unit = {},
    val onSecureNoteTypeChanged: (SyncObject.SecureNoteType) -> Unit = {}
)

data class LoginActions(
    val onValueChanged: (Data<CredentialFormData>) -> Unit = {},
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