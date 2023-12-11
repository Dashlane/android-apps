package com.dashlane.collections.sharing

sealed class NewCollectionShareViewState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData = ViewData()) : NewCollectionShareViewState()

    data class List(override val viewData: ViewData) : NewCollectionShareViewState()

    data class SharingSuccess(override val viewData: ViewData = ViewData()) :
        NewCollectionShareViewState()

    data class SharingFailed(override val viewData: ViewData = ViewData()) :
        NewCollectionShareViewState()

    data class ViewData(
        val userGroups: kotlin.collections.List<UserGroup> = emptyList(),
        val individuals: kotlin.collections.List<Individual> = emptyList(),
        val showSharingButton: Boolean = false,
        val sharedCollectionId: String? = null,
        val collectionName: String? = null,
        val showSearch: Boolean = false
    )

    data class UserGroup(
        val groupId: String,
        val name: String,
        val membersCount: Int,
        val selected: Boolean = false
    )

    data class Individual(val username: String, val selected: Boolean = false)
}