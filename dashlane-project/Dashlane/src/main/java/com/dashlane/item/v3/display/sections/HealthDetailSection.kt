package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.DisplayFieldMood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.PasswordHealthData
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.widgets.view.CircularProgressIndicator

@Composable
fun HealthDetailSection(data: CredentialFormData, editMode: Boolean) {
    if (editMode || data.passwordHealth?.isPasswordEmpty == true) return
    SectionContent(editMode = false) {
        SectionTitle(title = stringResource(id = R.string.vault_health_detail), editMode = false)
        if (data.passwordHealth != null) {
            val passwordScore = data.passwordHealth.passwordStrength?.score
            if (passwordScore != null) {
                DisplayField(
                    label = stringResource(id = R.string.vault_password_strength),
                    value = passwordScore.getShortTitle(LocalContext.current),
                    mood = getPasswordStrengthDisplayMood(passwordScore),
                )
            }
            if (data.passwordHealth.reusedCount > 1) {
                DisplayField(
                    label = stringResource(id = R.string.vault_password_safety),
                    value = pluralStringResource(
                        id = R.plurals.vault_password_safety_reused,
                        count = data.passwordHealth.reusedCount,
                        data.passwordHealth.reusedCount
                    ),
                    mood = getReusedCountDisplayMood(reusedCount = data.passwordHealth.reusedCount)
                )
            }
            if (data.passwordHealth.isCompromised) {
                DisplayField(
                    label = stringResource(id = R.string.vault_password_safety),
                    value = stringResource(R.string.vault_password_compromised),
                    mood = DisplayFieldMood.Danger(true)
                )
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun getPasswordStrengthDisplayMood(passwordStrengthScore: PasswordStrengthScore) =
    when (passwordStrengthScore) {
        PasswordStrengthScore.VERY_UNGUESSABLE,
        PasswordStrengthScore.SAFELY_UNGUESSABLE -> DisplayFieldMood.Positive(true)
        PasswordStrengthScore.SOMEWHAT_GUESSABLE -> DisplayFieldMood.Warning(true)
        else -> DisplayFieldMood.Danger(true)
    }

@Composable
private fun getReusedCountDisplayMood(reusedCount: Int) = when {
    reusedCount > 10 -> DisplayFieldMood.Danger(true)
    reusedCount > 1 -> DisplayFieldMood.Warning(true)
    else -> DisplayFieldMood.Positive(true)
}

@Preview
@Composable
private fun HealthDetailsSectionPreview() {
    DashlanePreview {
        HealthDetailSection(
            data = CredentialFormData(
                name = "My item",
                space = TeamSpace.Personal,
                passwordHealth = PasswordHealthData(
                    passwordStrength = PasswordStrength(
                        PasswordStrengthScore.SAFELY_UNGUESSABLE,
                        warning = null,
                        suggestions = listOf(),
                    ),
                    isCompromised = true,
                    isPasswordEmpty = false,
                    reusedCount = 2
                )
            ),
            editMode = false
        )
    }
}