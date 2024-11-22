package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.PasswordHealthData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.LinkedServicesField
import com.dashlane.item.v3.display.fields.PasswordField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.display.fields.TwoFactorAuthenticatorField
import com.dashlane.item.v3.display.forms.LoginActions
import com.dashlane.item.v3.display.forms.PasswordActions
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.url.name
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.clipboard.vault.CopyField

@SuppressWarnings("LongMethod")
@Composable
fun LoginDetailSection(
    data: Data<CredentialFormData>,
    isNewItem: Boolean,
    revealedFields: Set<SensitiveField>,
    editMode: Boolean,
    passwordActions: PasswordActions,
    loginActions: LoginActions
) {
    val showFieldOnEdit = editMode && data.commonData.isEditable
    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_login_detail_title), editMode = editMode)
        if (!data.formData.email.isNullOrBlank() || showFieldOnEdit && !isNewItem) {
            GenericField(
                label = stringResource(id = R.string.email_address),
                data = data.formData.email,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                keyboardType = KeyboardType.Email,
                onValueChanged = { value ->
                    loginActions.onValueChanged(
                        data.copyFormData {
                            it.copy(email = value)
                        }
                    )
                },
                onValueCopy = {
                    loginActions.onValueCopy(CopyField.Email)
                }.takeIf { data.commonData.isCopyActionAllowed }
            )
        }
        if (!data.formData.login.isNullOrBlank() || showFieldOnEdit) {
            GenericField(
                label = stringResource(id = R.string.authentifiant_hint_login),
                data = data.formData.login,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                onValueChanged = { value ->
                    loginActions.onValueChanged(data.copyFormData { it.copy(login = value) })
                },
                onValueCopy = {
                    loginActions.onValueCopy(CopyField.Login)
                }.takeIf { data.commonData.isCopyActionAllowed }
            )
        }
        if ((!data.formData.login.isNullOrBlank() && showFieldOnEdit) || !data.formData.secondaryLogin.isNullOrBlank()) {
            GenericField(
                label = stringResource(id = R.string.authentifiant_hint_secondary_login),
                data = data.formData.secondaryLogin,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                onValueChanged = { value ->
                    loginActions.onValueChanged(data.copyFormData { it.copy(secondaryLogin = value) })
                },
                onValueCopy = {
                    loginActions.onValueCopy(CopyField.SecondaryLogin)
                }.takeIf { data.commonData.isCopyActionAllowed }
            )
        }
        PasswordField(
            data = data,
            revealedFields = revealedFields,
            editMode = editMode,
            passwordActions = passwordActions
        )
        TwoFactorAuthenticatorField(
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            otp = data.formData.otp,
            onSetupTwoFactorAuthenticator = loginActions.onSetupTwoFactorAuthenticator,
            onRemoveTwoFactorAuthenticator = loginActions.onRemoveTwoFactorAuthenticator,
            onValueCopy = {
                loginActions.onValueCopy(CopyField.OtpCode)
            }.takeIf { data.commonData.isCopyActionAllowed },
            onHotpRefreshed = loginActions.onHotpRefreshed
        )
        GenericField(
            label = stringResource(id = R.string.authentifiant_hint_url),
            data = getWebsiteUrl(data.formData.url, editMode),
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            onValueChanged = { value ->
                loginActions.onValueChanged(data.copyFormData { it.copy(url = value) })
            },
            onValueOpen = loginActions.onWebsiteOpen
        )
        LinkedServicesField(
            linkedServices = data.formData.linkedServices,
            editMode = editMode,
            isEditable = data.commonData.isEditable,
            loginActions = loginActions
        )
        if (!data.formData.note.isNullOrBlank() || showFieldOnEdit) {
            GenericField(
                label = stringResource(id = R.string.authentifiant_hint_note),
                multiLine = true,
                data = data.formData.note,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                onValueChanged = { value ->
                    loginActions.onValueChanged(data.copyFormData { it.copy(note = value) })
                },
            )
        }
    }
}

private fun getWebsiteUrl(url: String?, editMode: Boolean): String? {
    return url?.let { fullUrl ->
        if (editMode) {
            
            fullUrl
        } else {
            
            fullUrl.toUrlOrNull()?.name ?: fullUrl 
        }
    }
}

@Preview
@Composable
private fun LoginDetailSectionPreview() = DashlanePreview {
    LoginDetailSection(
        data = Data(
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
                isCopyActionAllowed = true,
                space = TeamSpace.Personal
            )
        ),
        isNewItem = false,
        revealedFields = emptySet(),
        editMode = false,
        passwordActions = PasswordActions(),
        loginActions = LoginActions()
    )
}