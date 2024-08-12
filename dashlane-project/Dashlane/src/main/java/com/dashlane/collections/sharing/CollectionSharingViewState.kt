package com.dashlane.collections.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.Permission

sealed class CollectionSharingViewState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData = ViewData()) : CollectionSharingViewState()

    data class ShowList(override val viewData: ViewData) : CollectionSharingViewState()

    data class SharingSuccess(override val viewData: ViewData = ViewData()) :
        CollectionSharingViewState()

    data class SharingFailed(override val viewData: ViewData = ViewData()) :
        CollectionSharingViewState()

    data class SharingRestricted(override val viewData: ViewData = ViewData()) :
        CollectionSharingViewState()

    data class MyselfRevoked(override val viewData: ViewData = ViewData()) :
        CollectionSharingViewState()

    data class ConfirmRevoke(
        override val viewData: ViewData,
        val groupToRevoke: UserGroup? = null,
        val userToRevoke: Individual? = null
    ) : CollectionSharingViewState()

    data class ViewData(
        val userGroups: List<UserGroup> = emptyList(),
        val individuals: List<Individual> = emptyList(),
        val showSharingButton: Boolean = false,
        val sharedCollectionId: String? = null,
        val collectionName: String? = null,
        val showSearch: Boolean = false,
        val isAdmin: Boolean = false,
        val showSharingLimit: Boolean = false
    )

    data class UserGroup(
        val groupId: String,
        val name: String,
        val membersCount: Int,
        val permission: Permission = Permission.ADMIN,
        val selected: Boolean = false
    )

    data class Individual(
        val username: String,
        val permission: Permission = Permission.ADMIN,
        val selected: Boolean = false,
        val accepted: Boolean = true
    )
}