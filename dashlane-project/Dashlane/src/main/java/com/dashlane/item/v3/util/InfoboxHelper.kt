package com.dashlane.item.v3.util

import com.dashlane.R
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.InfoBoxData
import com.dashlane.item.v3.data.InfoboxStyle
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.ui.model.TextResource


internal fun <T : FormData> ItemEditState<T>.buildInfoBoxToDisplay(
    isFrozenState: Boolean,
    passwordLimitCount: Long?,
    locationPermissionEnabled: Boolean = false,
    locationEnabled: Boolean = false,
    isWiFiEnabled: Boolean = false,
): ItemEditState<T> {
    val isEditMode = isEditMode
    val isNew = isNew
    datas ?: return this
    val infoBoxes = mutableListOf<InfoBoxData>().apply {
        addGeneralInfoBoxes(
            isEditMode = isEditMode,
            isFrozenState = isFrozenState,
            passwordLimitCount = passwordLimitCount,
        )
        when (datas.current.formData) {
            is CredentialFormData -> addCredentialInfoBoxes(
                commonData = datas.current.commonData,
                data = datas.current.formData,
                isEditMode = isEditMode,
            )
            is SecureNoteFormData -> addSecureNoteInfoBoxes(
                commonData = datas.current.commonData,
                isEditMode = isEditMode,
            )
            is CreditCardFormData -> addCreditCardInfoBoxes(
                isNew = isNew,
                isEditMode = isEditMode,
                isNfcAvailable = datas.current.formData.isNfcAvailable,
            )
            is WiFiFormData -> addWiFiInfoBoxes(
                isEditMode = isEditMode,
                isLocationEnabled = locationEnabled,
                isLocationPermissionEnabled = locationPermissionEnabled,
                isWiFiEnabled = isWiFiEnabled,
            )
            else -> {
                
            }
        }
    }
    return copy(infoBoxes = infoBoxes)
}

private fun MutableList<InfoBoxData>.addGeneralInfoBoxes(
    isEditMode: Boolean,
    isFrozenState: Boolean,
    passwordLimitCount: Long?,
) {
    if (isEditMode) return

    
    if (isFrozenState) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.frozen_account_item_edit_banner_title),
                description = TextResource.StringText(
                    stringRes = R.string.frozen_account_item_edit_banner_description,
                    arg = TextResource.Arg.StringArg(passwordLimitCount.toString()),
                ),
                mood = Mood.Danger,
            ),
        )
    }
}

private fun MutableList<InfoBoxData>.addCredentialInfoBoxes(
    isEditMode: Boolean,
    commonData: CommonData,
    data: CredentialFormData,
) {
    if (isEditMode) return

    
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
                ),
            ),
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
                ),
            ),
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
                    action = InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE,
                ),
            ),
        )
    }

    
    if (commonData.isSharedWithLimitedRight) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
                description = TextResource.StringText(R.string.vault_limited_rights_infobox_content),
                mood = Mood.Neutral,
            ),
        )
    }
}

private fun MutableList<InfoBoxData>.addSecureNoteInfoBoxes(
    isEditMode: Boolean,
    commonData: CommonData,
) {
    if (isEditMode) return

    
    if (commonData.isSharedWithLimitedRight) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.vault_limited_rights_infobox_title),
                description = TextResource.StringText(R.string.vault_limited_rights_secure_note_infobox_content),
                mood = Mood.Neutral,
                infoboxStyle = InfoboxStyle.MEDIUM,
            ),
        )
    } else if (commonData.isShared || commonData.collections?.isNotEmpty() == true) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.secure_note_attachments_restriction_infobox_title),
                mood = Mood.Neutral,
                infoboxStyle = InfoboxStyle.MEDIUM,
            ),
        )
    }
    if (commonData.attachmentCount > 0) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.secure_note_sharing_and_collection_restriction_infobox_title),
                mood = Mood.Neutral,
                infoboxStyle = InfoboxStyle.MEDIUM,
            ),
        )
    }
}

private fun MutableList<InfoBoxData>.addCreditCardInfoBoxes(
    isNew: Boolean,
    isEditMode: Boolean,
    isNfcAvailable: Boolean,
) {
    if (isNew && isEditMode && isNfcAvailable) {
        add(
            InfoBoxData(
                title = TextResource.StringText(R.string.credit_card_nfc_infobox_title),
                description = TextResource.StringText(R.string.credit_card_nfc_infobox_description),
                mood = Mood.Brand,
                infoboxStyle = InfoboxStyle.MEDIUM,
            ),
        )
    }
}

private fun MutableList<InfoBoxData>.addWiFiInfoBoxes(
    isEditMode: Boolean,
    isLocationEnabled: Boolean,
    isLocationPermissionEnabled: Boolean,
    isWiFiEnabled: Boolean,
) {
    if (isEditMode) {
        when {
            !isWiFiEnabled -> {
                add(
                    InfoBoxData(
                        title = TextResource.StringText(R.string.wifi_disable_infobox_title),
                        button = InfoBoxData.Button(
                            text = TextResource.StringText(R.string.wifi_infobox_open_settings),
                            action = InfoBoxData.Button.Action.OPEN_NETWORK_SETTINGS,
                        ),
                        mood = Mood.Neutral,
                        infoboxStyle = InfoboxStyle.LARGE,
                    ),
                )
            }
            !isLocationEnabled -> {
                add(
                    InfoBoxData(
                        title = TextResource.StringText(R.string.location_disable_infobox_title),
                        button = InfoBoxData.Button(
                            text = TextResource.StringText(R.string.wifi_infobox_open_settings),
                            action = InfoBoxData.Button.Action.OPEN_LOCATION_SETTINGS,
                        ),
                        mood = Mood.Neutral,
                        infoboxStyle = InfoboxStyle.LARGE,
                    ),
                )
            }
            !isLocationPermissionEnabled -> {
                add(
                    InfoBoxData(
                        title = TextResource.StringText(R.string.wifi_missing_location_permission_infobox_title),
                        button = InfoBoxData.Button(
                            text = TextResource.StringText(R.string.wifi_missing_location_permission_infobox_button),
                            action = InfoBoxData.Button.Action.ENABLE_LOCATION_PERMISSION,
                        ),
                        mood = Mood.Neutral,
                        infoboxStyle = InfoboxStyle.LARGE,
                    ),
                )
            }
        }
    }
}