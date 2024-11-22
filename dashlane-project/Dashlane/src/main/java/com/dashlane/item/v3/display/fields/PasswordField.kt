package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ObfuscatedDisplayField
import com.dashlane.design.component.ObfuscatedField
import com.dashlane.design.component.PasswordDisplayField
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.PasswordFieldFeedback
import com.dashlane.design.component.PasswordStrengthIndicator
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.PasswordHealthData
import com.dashlane.item.v3.display.forms.PasswordActions
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.SensitiveField.PASSWORD
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.xml.domain.SyncObfuscatedValue

@Composable
internal fun PasswordField(
    data: Data<CredentialFormData>,
    revealedFields: Set<SensitiveField>,
    editMode: Boolean,
    passwordActions: PasswordActions
) {
    if (editMode) {
        PasswordFieldEdit(data, passwordActions)
    } else {
        PasswordFieldDisplay(data, revealedFields, passwordActions)
    }
}

@Composable
private fun PasswordFieldEdit(
    data: Data<CredentialFormData>,
    passwordActions: PasswordActions
) {
    var obfuscated by rememberSaveable { mutableStateOf(true) }
    val passwordFeedback = data.formData.passwordHealth?.passwordStrength?.let {
        PasswordFieldFeedback.Strength(value = it.toPasswordStrengthIndicator())
    }
    if (data.commonData.isEditable) {
        PasswordField(
            modifier = Modifier.fillMaxWidth(),
            value = data.formData.password?.value?.toString() ?: "",
            onValueChange = passwordActions.onPasswordUpdate,
            label = stringResource(id = R.string.authentifiant_hint_password),
            feedback = passwordFeedback,
            obfuscated = obfuscated,
            readOnly = false,
            actions = TextFieldActions.Password(
                hideRevealAction = FieldAction.HideReveal(
                    contentDescriptionToHide = stringResource(id = R.string.and_accessibility_text_edit_hide),
                    contentDescriptionToReveal = stringResource(id = R.string.and_accessibility_text_edit_reveal),
                    onClick = {
                        obfuscated = !obfuscated
                        true
                    }
                ),
                passwordGeneratorAction =
                FieldAction.Generate(
                    contentDescription = stringResource(id = R.string.generate),
                    onClick = {
                        passwordActions.onGeneratePassword()
                        true
                    }
                ),
                genericAction = null,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
    } else {
        ObfuscatedField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(id = R.string.authentifiant_hint_password),
            value = "••••••••",
            limitedRightsDescription = stringResource(id = R.string.reveal_password_permission_body)
        )
    }
}

@Composable
private fun PasswordFieldDisplay(
    data: Data<CredentialFormData>,
    revealedFields: Set<SensitiveField>,
    passwordActions: PasswordActions
) {
    val isPasswordRevealed = revealedFields.contains(PASSWORD)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (!data.commonData.isSharedWithLimitedRight) {
            PasswordDisplayField(
                label = stringResource(id = R.string.authentifiant_hint_password),
                revealedValue = data.formData.password?.value?.toString(),
                isEmpty = data.formData.passwordHealth?.isPasswordEmpty ?: false,
                obfuscated = !isPasswordRevealed,
                action1 = FieldAction.HideReveal(
                    contentDescriptionToHide = stringResource(id = R.string.and_accessibility_text_edit_hide),
                    contentDescriptionToReveal = stringResource(id = R.string.and_accessibility_text_edit_reveal),
                    onClick = {
                        if (isPasswordRevealed) {
                            passwordActions.onHidePassword()
                        } else {
                            passwordActions.onShowPassword()
                        }
                        return@HideReveal true
                    }
                ),
                action2 = FieldAction.Generic(
                    iconLayout = ButtonLayout.IconOnly(
                        iconToken = IconTokens.actionCopyOutlined,
                        contentDescription = stringResource(id = R.string.and_accessibility_copy_password)
                    ),
                    onClick = {
                        passwordActions.onCopyPassword()
                        return@Generic true
                    }
                ).takeIf { data.commonData.isCopyActionAllowed }
            )
        } else {
            ObfuscatedDisplayField(
                label = stringResource(id = R.string.authentifiant_hint_password),
                limitedRightsDescription = stringResource(id = R.string.reveal_password_permission_title),
                actions = DisplayFieldActions.newInstance(
                    action1 = FieldAction.Generic(
                        iconLayout = ButtonLayout.IconOnly(
                            iconToken = IconTokens.feedbackInfoOutlined,
                            contentDescription = stringResource(id = R.string.and_accessibility_info)
                        ),
                        onClick = {
                            passwordActions.onLimitedRightInfoOpen()
                            return@Generic true
                        }
                    )
                )
            )
        }
    }
}

private fun PasswordStrength.toPasswordStrengthIndicator(): PasswordStrengthIndicator.Strength {
    return when (this.score) {
        PasswordStrengthScore.TOO_GUESSABLE -> PasswordStrengthIndicator.Strength.WEAKEST
        PasswordStrengthScore.VERY_GUESSABLE -> PasswordStrengthIndicator.Strength.WEAK
        PasswordStrengthScore.SOMEWHAT_GUESSABLE -> PasswordStrengthIndicator.Strength.ACCEPTABLE
        PasswordStrengthScore.SAFELY_UNGUESSABLE -> PasswordStrengthIndicator.Strength.GOOD
        PasswordStrengthScore.VERY_UNGUESSABLE -> PasswordStrengthIndicator.Strength.STRONG
    }
}

@Preview
@Composable
@Suppress("LongMethod")
private fun PasswordFieldPreview() {
    val credentialFormData = Data<CredentialFormData>(
        formData = CredentialFormData(
            password = CredentialFormData.Password(SyncObfuscatedValue("ThisIsStrong12"))
        ),
        commonData = CommonData()
    )
    DashlanePreview {
        Column {
            PasswordField(
                data = credentialFormData,
                revealedFields = setOf(),
                editMode = true,
                passwordActions = PasswordActions()
            )
            PasswordField(
                data = credentialFormData.copyCommonData { it.copy(isEditable = true) },
                revealedFields = setOf(),
                editMode = true,
                passwordActions = PasswordActions()
            )
            PasswordField(
                data = credentialFormData,
                revealedFields = setOf(),
                editMode = false,
                passwordActions = PasswordActions()
            )
            PasswordField(
                data = credentialFormData.copyCommonData { it.copy(isEditable = true) },
                revealedFields = setOf(),
                editMode = false,
                passwordActions = PasswordActions()
            )
            
            PasswordField(
                data = credentialFormData.copyCommonData { it.copy(isSharedWithLimitedRight = true) },
                revealedFields = setOf(PASSWORD),
                editMode = false,
                passwordActions = PasswordActions()
            )
            
            PasswordField(
                data = credentialFormData.copyCommonData { it.copy(isEditable = false, isCopyActionAllowed = false) },
                revealedFields = setOf(PASSWORD),
                editMode = false,
                passwordActions = PasswordActions()
            )
            PasswordField(
                data = credentialFormData.copyFormData {
                    it.copy(
                        passwordHealth = PasswordHealthData(
                            passwordStrength = PasswordStrength(
                                PasswordStrengthScore.SAFELY_UNGUESSABLE,
                                warning = null,
                                suggestions = listOf()
                            ),
                            isCompromised = false,
                            reusedCount = 0,
                            isPasswordEmpty = false
                        )
                    )
                }.copyCommonData {
                    it.copy(isEditable = true)
                },
                revealedFields = setOf(),
                editMode = true,
                passwordActions = PasswordActions()
            )
        }
    }
}