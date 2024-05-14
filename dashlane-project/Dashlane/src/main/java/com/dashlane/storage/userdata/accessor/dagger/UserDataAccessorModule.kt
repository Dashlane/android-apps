package com.dashlane.storage.userdata.accessor.dagger

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
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.DataSaverImpl
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.FrequentSearchRacletteImpl
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQuery
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQueryImpl
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQueryImplRaclette
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplRaclette
import com.dashlane.sync.vault.SyncVault
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UserDataAccessorModule {
    @Binds
    fun bindDataSaver(impl: DataSaverImpl): DataSaver

    @Binds
    fun bindFrequentSearch(impl: FrequentSearchRacletteImpl): FrequentSearch

    @Binds
    fun bindVaultDataQuery(impl: VaultDataQueryImplRaclette): VaultDataQuery

    @Binds
    fun bindGenericDataQuery(impl: GenericDataQueryImplRaclette): GenericDataQuery

    @Binds
    fun bindCollectionDataQuery(impl: CollectionDataQueryImpl): CollectionDataQuery

    @Binds
    fun bindCredentialDataQuery(impl: CredentialDataQueryImplRaclette): CredentialDataQuery

    @Binds
    fun bindGeneratedPasswordQuery(impl: GeneratedPasswordQueryImpl): GeneratedPasswordQuery

    @Binds
    fun bindDataCounter(impl: DataCounterRacletteImpl): DataCounter

    @Binds
    fun bindDataChangeHistoryQuery(impl: DataChangeHistoryQueryRacletteImpl): DataChangeHistoryQuery

    @Binds
    fun bindSharingDao(impl: SharingDaoImplRaclette): SharingDao

    @Binds
    fun bindDataChangeHistorySaver(impl: DataChangeHistorySaverRaclette): DataChangeHistorySaver

    @Binds
    fun bindDatabaseItemSaver(impl: DatabaseItemSaverRaclette): DatabaseItemSaver

    @Binds
    fun bindSyncVault(impl: SyncVaultImplRaclette): SyncVault
}