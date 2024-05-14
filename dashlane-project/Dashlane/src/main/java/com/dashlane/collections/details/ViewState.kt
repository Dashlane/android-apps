package com.dashlane.collections.details

import com.dashlane.collections.SpaceData
import com.dashlane.url.UrlDomain

sealed class ViewState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData) : ViewState()
    data class List(override val viewData: ViewData) : ViewState()
    data class Empty(override val viewData: ViewData) : ViewState()
    data class DeletePrompt(override val viewData: ViewData) : ViewState()
    data class Deleted(override val viewData: ViewData) : ViewState()
}

data class ViewData(
    val collectionName: String?,
    val shared: Boolean,
    val canRemoveFromSharedCollection: Boolean,
    val items: List<SummaryForUi>,
    val spaceData: SpaceData?,
    val collectionLimit: CollectionLimiter.UserLimit = CollectionLimiter.UserLimit.NO_LIMIT
)

data class SummaryForUi(
    val id: String,
    val type: String,
    val thumbnail: UrlDomain?,
    val firstLine: String,
    val secondLine: String,
    val sharingPermission: String?,
    val spaceData: SpaceData?
)