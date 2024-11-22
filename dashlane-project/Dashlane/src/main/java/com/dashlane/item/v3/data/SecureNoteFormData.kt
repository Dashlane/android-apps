package com.dashlane.item.v3.data

import com.dashlane.item.v3.data.FormData.SharingCount
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.SecureNoteItemEditViewModel.Companion.SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT
import com.dashlane.securefile.extensions.attachmentsCount
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

data class SecureNoteFormData(
    val content: String? = null,
    val contentFeedback: SecureNoteContentFeedback? = null,
    val secureSettingAvailable: Boolean = true,
    val secured: Boolean = false,
    val secureNoteType: SyncObject.SecureNoteType = SyncObject.SecureNoteType.NO_TYPE
) : FormData

sealed class SecureNoteContentFeedback {
    abstract val error: Boolean

    data class UnderLimitFeedback(val length: Int, val limit: Int) : SecureNoteContentFeedback() {
        override val error: Boolean = false
    }

    data class AtLimitFeedback(val limit: Int) : SecureNoteContentFeedback() {
        override val error: Boolean = false
    }

    data class AboveLimitFeedback(val length: Int, val limit: Int) : SecureNoteContentFeedback() {
        override val error: Boolean = true
    }

    companion object {
        fun fromContent(content: String?): SecureNoteContentFeedback {
            val length = content?.length ?: 0
            return if (length < SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT) {
                UnderLimitFeedback(
                    length = length,
                    limit = SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT
                )
            } else if (length == SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT) {
                AtLimitFeedback(limit = SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT)
            } else {
                AboveLimitFeedback(
                    length = length,
                    limit = SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT
                )
            }
        }
    }
}

internal fun SummaryObject.SecureNote.toSecureNoteFormData(
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
    formData = SecureNoteFormData(
        content = this.content,
        contentFeedback = SecureNoteContentFeedback.fromContent(this.content),
        secureSettingAvailable = secureSettingAvailable,
        secured = this.secured ?: false,
        secureNoteType = this.type ?: SyncObject.SecureNoteType.NO_TYPE
    )
)