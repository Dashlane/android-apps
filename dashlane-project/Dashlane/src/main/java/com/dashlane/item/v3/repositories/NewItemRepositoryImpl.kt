package com.dashlane.item.v3.repositories

import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.toAuthentifiant
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class NewItemRepositoryImpl @Inject constructor(
    private val appLinkDownloader: AuthentifiantAppLinkDownloader,
    private val dataSaver: DataSaver
) : NewItemRepository {
    @Suppress("UNCHECKED_CAST")
    override suspend fun performAdditionalSteps(itemToSave: VaultItem<SyncObject>) {
        when (itemToSave.syncObject) {
            is SyncObject.Authentifiant -> {
                itemToSave as VaultItem<SyncObject.Authentifiant>
                appLinkDownloader.fetch(itemToSave.toSummary())
            }

            is SyncObject.PaymentPaypal -> {
                itemToSave as VaultItem<SyncObject.PaymentPaypal>
                
                val credential = itemToSave.toAuthentifiant()
                dataSaver.save(credential)
            }

            else -> {
                
            }
        }
    }
}