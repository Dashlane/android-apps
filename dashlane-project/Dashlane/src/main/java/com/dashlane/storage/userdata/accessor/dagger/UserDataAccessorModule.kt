package com.dashlane.storage.userdata.accessor.dagger

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingDaoImplRaclette
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.DatabaseItemSaverRaclette
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.CollectionDataQueryImpl
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.CredentialDataQueryImpl
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQueryImpl
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaver
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaverImpl
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.DataCounterImpl
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.DataSaverImpl
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.FrequentSearchImpl
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQuery
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQueryImpl
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQueryImpl
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQueryImpl
import com.dashlane.sync.SyncVaultImplRaclette
import com.dashlane.sync.vault.SyncVault
import com.dashlane.vault.util.AuthentifiantPackageNameMatcher
import com.dashlane.vault.util.AuthentifiantPackageNameMatcherImpl
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
    fun bindFrequentSearch(impl: FrequentSearchImpl): FrequentSearch

    @Binds
    fun bindVaultDataQuery(impl: VaultDataQueryImpl): VaultDataQuery

    @Binds
    fun bindGenericDataQuery(impl: GenericDataQueryImpl): GenericDataQuery

    @Binds
    fun bindCollectionDataQuery(impl: CollectionDataQueryImpl): CollectionDataQuery

    @Binds
    fun bindCredentialDataQuery(impl: CredentialDataQueryImpl): CredentialDataQuery

    @Binds
    fun bindGeneratedPasswordQuery(impl: GeneratedPasswordQueryImpl): GeneratedPasswordQuery

    @Binds
    fun bindDataCounter(impl: DataCounterImpl): DataCounter

    @Binds
    fun bindDataChangeHistoryQuery(impl: DataChangeHistoryQueryImpl): DataChangeHistoryQuery

    @Binds
    fun bindSharingDao(impl: SharingDaoImplRaclette): SharingDao

    @Binds
    fun bindDataChangeHistorySaver(impl: DataChangeHistorySaverImpl): DataChangeHistorySaver

    @Binds
    fun bindDatabaseItemSaver(impl: DatabaseItemSaverRaclette): DatabaseItemSaver

    @Binds
    fun bindSyncVault(impl: SyncVaultImplRaclette): SyncVault

    @Binds
    fun bindAuthentifiantPackageNameMatcher(impl: AuthentifiantPackageNameMatcherImpl): AuthentifiantPackageNameMatcher
}