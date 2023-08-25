package com.dashlane.ui.screens.fragments.userdata.sharing

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import javax.inject.Inject

class SharingRevokeMe @Inject constructor(
    private val sharingDataProvider: SharingDataProvider
) {
    suspend fun execute(
        itemGroup: ItemGroup,
        vaultItem: VaultItem<*>
    ) {
        sharingDataProvider.declineItemGroupInvite(itemGroup, vaultItem.toSummary(), true)
    }
}