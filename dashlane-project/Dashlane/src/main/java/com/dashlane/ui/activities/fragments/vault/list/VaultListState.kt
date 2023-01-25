package com.dashlane.ui.activities.fragments.vault.list

sealed class VaultListState {
    abstract val data: VaultListData

    data class EmptyInfo(override val data: VaultListData, val displayEmptyInfo: Boolean = false) : VaultListState()

    data class ItemList(override val data: VaultListData) : VaultListState()

    data class Refreshing(override val data: VaultListData) : VaultListState()
}