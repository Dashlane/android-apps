package com.dashlane.storage

import com.dashlane.core.sharing.SharingDao
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaver
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.vault.SyncVault

interface DataStorageProvider {
    val useRaclette: Boolean
    val vaultDataQuery: VaultDataQuery
    val genericDataQuery: GenericDataQuery
    val credentialDataQuery: CredentialDataQuery
    val syncVault: SyncVault
    val itemSaver: DatabaseItemSaver
    val sharingDao: SharingDao
    val frequentSearch: FrequentSearch
    val dataCounter: DataCounter
    val dataChangeHistorySaver: DataChangeHistorySaver
    val dataChangeHistoryQuery: DataChangeHistoryQuery
}