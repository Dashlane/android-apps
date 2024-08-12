package com.dashlane.item.v3.util

import com.dashlane.R
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.InfoBoxData
import com.dashlane.item.v3.viewmodels.State
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.ui.model.TextResource

internal fun State.buildInfoBoxToDisplay(isFrozenState: Boolean, passwordLimitCount: Long?): State {
    val data = formData
    val infoBoxes = mutableListOf<InfoBoxData>().apply {
        addGeneralInfoBoxes(isFrozenState, passwordLimitCount)
        when (data) {
            is CredentialFormData -> addCredentialInfoBoxes(data)
            else -> {
                
            }
        }
    }
    return copy(infoBoxes = infoBoxes)
}

private fun MutableList<InfoBoxData>.addGeneralInfoBoxes(
    isFrozenState: Boolean,
    passwordLimitCount: Long?
) {
    
    if (isFrozenState) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.frozen_account_item_edit_banner_title),
                description = TextResource.StringText(
                    stringRes = R.string.frozen_account_item_edit_banner_description,
                    arg = TextResource.Arg.StringArg(passwordLimitCount.toString())
                ),
                mood = Mood.Danger
            )
        )
    }
}

private fun MutableList<InfoBoxData>.addCredentialInfoBoxes(data: CredentialFormData) {
    
    val score = data.passwordHealth?.passwordStrength?.score
    if (score == PasswordStrengthScore.VERY_GUESSABLE || score == PasswordStrengthScore.TOO_GUESSABLE) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.weak_password_info_title),
                description = TextResource.StringText(R.string.weak_password_info_content),
                mood = Mood.Warning,
                button = InfoBoxData.Button(
                    text = TextResource.StringText(R.string.weak_password_info_action),
                    action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE,
                )
            )
        )
    }

    
    if (data.passwordHealth?.isCompromised == true) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.compromised_password_info_title),
                description = TextResource.StringText(R.string.compromised_password_info_content),
                mood = Mood.Warning,
                button = InfoBoxData.Button(
                    text = TextResource.StringText(R.string.compromised_password_info_action),
                    action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE,
                )
            )
        )
    }

    
    if ((data.passwordHealth?.reusedCount ?: 0) > 2) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.reused_password_info_title),
                description = TextResource.StringText(R.string.reused_password_info_content),
                mood = Mood.Warning,
                button = InfoBoxData.Button(
                    text = TextResource.StringText(R.string.weak_password_info_action),
                    action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE
                )
            )
        )
    }

    
    if (data.isSharedWithLimitedRight) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
                description = TextResource.StringText(R.string.vault_limited_rights_infobox_content),
                mood = Mood.Neutral
            )
        )
    }
}