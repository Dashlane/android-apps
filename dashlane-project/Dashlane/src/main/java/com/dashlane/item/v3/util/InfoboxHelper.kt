package com.dashlane.item.v3.util

import com.dashlane.R
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.InfoBoxData
import com.dashlane.item.v3.data.InfoboxStyle
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.ui.model.TextResource


internal fun <T : FormData> ItemEditState<T>.buildInfoBoxToDisplay(
    isFrozenState: Boolean,
    passwordLimitCount: Long?
): ItemEditState<T> {
    datas ?: return this
    val infoBoxes = mutableListOf<InfoBoxData>().apply {
        addGeneralInfoBoxes(isFrozenState, passwordLimitCount)
        when (datas.current.formData) {
            is CredentialFormData -> addCredentialInfoBoxes(datas.current.commonData, datas.current.formData)
            is SecureNoteFormData -> addSecureNoteInfoBoxes(datas.current.commonData)
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

private fun MutableList<InfoBoxData>.addCredentialInfoBoxes(commonData: CommonData, data: CredentialFormData) {
    
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

    
    if (commonData.isSharedWithLimitedRight) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
                description = TextResource.StringText(R.string.vault_limited_rights_infobox_content),
                mood = Mood.Neutral
            )
        )
    }
}

private fun MutableList<InfoBoxData>.addSecureNoteInfoBoxes(commonData: CommonData) {
    
    if (commonData.isSharedWithLimitedRight) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
                description = TextResource.StringText(R.string.vault_limited_rights_secure_note_infobox_content),
                mood = Mood.Neutral,
                infoboxStyle = InfoboxStyle.MEDIUM
            )
        )
    } else if (commonData.isShared || commonData.collections?.isNotEmpty() == true) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.secure_note_attachments_restriction_infobox_title),
                mood = Mood.Neutral,
                infoboxStyle = InfoboxStyle.MEDIUM
            )
        )
    }
    if (commonData.attachmentCount > 0) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.secure_note_sharing_and_collection_restriction_infobox_title),
                mood = Mood.Neutral,
                infoboxStyle = InfoboxStyle.MEDIUM
            )
        )
    }
}