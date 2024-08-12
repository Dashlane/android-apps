package com.dashlane.collections.details

import com.dashlane.collections.SpaceData
import com.dashlane.xml.domain.SyncObject

sealed class ViewState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData) : ViewState()
    data class List(override val viewData: ViewData) : ViewState()
    data class Empty(override val viewData: ViewData) : ViewState()
    data class DeletePrompt(override val viewData: ViewData) : ViewState()
    data class RevokeAccessPrompt(override val viewData: ViewData) : ViewState()
    data class Deleted(override val viewData: ViewData) : ViewState()
    data class SharedCollectionDeleteError(override val viewData: ViewData) : ViewState()
    data class SharingWithAttachmentError(override val viewData: ViewData) : ViewState()
}

data class ViewData(
    val collectionName: String?,
    val shared: Boolean,
    val sections: List<ItemSection>,
    val spaceData: SpaceData?,
    val collectionLimit: CollectionLimiter.UserLimit = CollectionLimiter.UserLimit.NO_LIMIT,
    val editAllowed: Boolean,
    val deleteAllowed: Boolean,
    val hasItemWithAttachment: Boolean
)

data class SummaryForUi(
    val id: String,
    val type: String,
    val thumbnail: ThumbnailData,
    val firstLine: String,
    val secondLine: ContentLine2,
    val sharingPermission: String?,
    val spaceData: SpaceData?
)

data class ItemSection(
    val sectionType: SectionType,
    val items: List<SummaryForUi>
)

enum class SectionType(val index: Int) {
    SECTION_LOGIN(0),
    SECTION_SECURE_NOTE(1)
}

sealed class ContentLine2 {
    data class Text(val value: String) : ContentLine2()
    data object SecureNoteSecured : ContentLine2()
}

sealed class ThumbnailData {
    data class UrlThumbnail(val url: String?) : ThumbnailData()
    data class SecureNoteThumbnail(val secureNoteType: SyncObject.SecureNoteType) : ThumbnailData()
}