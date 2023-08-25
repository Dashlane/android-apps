package com.dashlane.storage

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingDaoImplRaclette
import com.dashlane.core.sync.SyncVaultImplRaclette
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.DatabaseItemSaverRaclette
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.CollectionDataQueryImpl
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.CredentialDataQueryImplRaclette
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQueryRacletteImpl
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaver
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaverRaclette
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.DataCounterRacletteImpl
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.FrequentSearchRacletteImpl
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQueryImplRaclette
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplRaclette
import com.dashlane.sync.vault.SyncVault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorageProviderImpl @Inject constructor(
    private val genericDataQueryNew: GenericDataQueryImplRaclette,
    private val syncVaultNew: SyncVaultImplRaclette,
    private val vaultDataQueryNew: VaultDataQueryImplRaclette,
    private val databaseItemSaverNew: DatabaseItemSaverRaclette,
    private val credentialDataQueryNew: CredentialDataQueryImplRaclette,
    private val collectionDataQueryNew: CollectionDataQueryImpl,
    private val sharingDaoRaclette: SharingDaoImplRaclette,
    private val frequentSearchRacletteImpl: FrequentSearchRacletteImpl,
    private val dataCounterRacletteImpl: DataCounterRacletteImpl,
    private val dataChangeHistorySaverRaclette: DataChangeHistorySaverRaclette,
    private val dataChangeHistoryQueryRaclette: DataChangeHistoryQueryRacletteImpl
) : DataStorageProvider {
    override val genericDataQuery: GenericDataQuery
        get() = genericDataQueryNew

    override val syncVault: SyncVault
        get() = syncVaultNew

    override val vaultDataQuery: VaultDataQuery
        get() = vaultDataQueryNew

    override val itemSaver: DatabaseItemSaver
        get() = databaseItemSaverNew

    override val credentialDataQuery: CredentialDataQuery
        get() = credentialDataQueryNew

    override val collectionDataQuery: CollectionDataQuery
        get() = collectionDataQueryNew

    override val sharingDao: SharingDao
        get() = sharingDaoRaclette

    override val frequentSearch: FrequentSearch
        get() = frequentSearchRacletteImpl

    override val dataCounter: DataCounter
        get() = dataCounterRacletteImpl

    override val dataChangeHistorySaver: DataChangeHistorySaver
        get() = dataChangeHistorySaverRaclette

    override val dataChangeHistoryQuery: DataChangeHistoryQuery
        get() = dataChangeHistoryQueryRaclette
}