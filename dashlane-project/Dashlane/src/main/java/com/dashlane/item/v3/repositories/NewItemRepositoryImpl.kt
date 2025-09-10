package com.dashlane.item.v3.repositories

import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class NewItemRepositoryImpl @Inject constructor(
    private val appLinkDownloader: AuthentifiantAppLinkDownloader,
) : NewItemRepository {
    @Suppress("UNCHECKED_CAST")
    override suspend fun performAdditionalSteps(itemToSave: VaultItem<SyncObject>) {
        when (itemToSave.syncObject) {
            is SyncObject.Authentifiant -> {
                itemToSave as VaultItem<SyncObject.Authentifiant>
                appLinkDownloader.fetch(itemToSave.toSummary())
            }

            else -> {
                
            }
        }
    }
}