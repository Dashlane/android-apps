package com.dashlane.item.v3.data

import com.dashlane.item.v3.data.FormData.SharingCount
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.securefile.extensions.attachmentsCount
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject

data class SecretFormData(
    val content: String? = null,
    val secureSettingAvailable: Boolean = true,
    val secured: Boolean = false,
) : FormData

internal fun SummaryObject.Secret.toSecretFormData(
    availableSpaces: List<TeamSpace>,
    isEditable: Boolean?,
    canDelete: Boolean?,
    sharingCount: SharingCount?,
    teamSpace: TeamSpace?,
    isSharedWithLimitedRight: Boolean,
    secureSettingAvailable: Boolean
) = Data(
    commonData = CommonData(
        id = this.id,
        name = this.title ?: "",
        isShared = this.isShared,
        isEditable = isEditable ?: false,
        canDelete = canDelete ?: false,
        sharingCount = sharingCount ?: SharingCount(),
        created = this.creationDatetime,
        updated = this.userModificationDatetime,
        space = teamSpace,
        availableSpaces = availableSpaces,
        isSharedWithLimitedRight = isSharedWithLimitedRight,
        attachmentCount = attachmentsCount(),
    ),
    formData = SecretFormData(
        content = this.content,
        secureSettingAvailable = secureSettingAvailable,
        secured = this.secured ?: false,
    )
)