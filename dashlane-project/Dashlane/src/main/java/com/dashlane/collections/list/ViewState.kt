package com.dashlane.collections.list

import com.dashlane.collections.SpaceData

sealed class ViewState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData) : ViewState()
    data class List(override val viewData: ViewData) : ViewState()
    data class Empty(override val viewData: ViewData) : ViewState()
    data class RevokeAccessPrompt(override val viewData: ViewData, val collectionId: String) : ViewState()
    data class SharedCollectionDeleteError(override val viewData: ViewData) : ViewState()
}

data class ViewData(
    val collections: List<CollectionViewData>
)

data class CollectionViewData(
    val id: String,
    val name: String,
    val itemCount: Int,
    val spaceData: SpaceData?,
    val shared: Boolean,
    val shareEnabled: Boolean,
    val shareAllowed: Boolean,
    val shareLimitedByTeam: Boolean,
    val editAllowed: Boolean,
    val deleteAllowed: Boolean
)
