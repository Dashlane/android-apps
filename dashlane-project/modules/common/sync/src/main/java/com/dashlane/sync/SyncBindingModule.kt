package com.dashlane.sync

import com.dashlane.sync.cryptochanger.RemoteKeyIdGenerator
import com.dashlane.sync.cryptochanger.RemoteKeyIdGeneratorImpl
import com.dashlane.sync.cryptochanger.SyncCryptoChanger
import com.dashlane.sync.cryptochanger.SyncCryptoChangerImpl
import com.dashlane.sync.domain.TransactionCipher
import com.dashlane.sync.domain.TransactionCipherImpl
import com.dashlane.sync.merger.SyncMerger
import com.dashlane.sync.merger.SyncMergerImpl
import com.dashlane.sync.repositories.ChronologicalSync
import com.dashlane.sync.repositories.ChronologicalSyncImpl
import com.dashlane.sync.repositories.SyncDeduplication
import com.dashlane.sync.repositories.SyncDeduplicationImpl
import com.dashlane.sync.repositories.SyncRepository
import com.dashlane.sync.repositories.SyncRepositoryImpl
import com.dashlane.sync.repositories.strategies.SyncServices
import com.dashlane.sync.repositories.strategies.SyncServicesImpl
import com.dashlane.sync.treat.TreatProblemManager
import com.dashlane.sync.treat.TreatProblemManagerImpl
import com.dashlane.sync.util.SyncLogs
import com.dashlane.sync.util.SyncLogsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SyncBindingModule {

    @Singleton
    @Binds
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    abstract fun bindSyncServices(impl: SyncServicesImpl): SyncServices

    @Singleton
    @Binds
    abstract fun bindTreatProblemManager(impl: TreatProblemManagerImpl): TreatProblemManager

    @Binds
    abstract fun bindDeduplication(impl: SyncDeduplicationImpl): SyncDeduplication

    @Singleton
    @Binds
    abstract fun bindSyncLogs(impl: SyncLogsImpl): SyncLogs

    @Singleton
    @Binds
    abstract fun bindSyncMerger(impl: SyncMergerImpl): SyncMerger

    @Binds
    abstract fun bindTransactionCipher(impl: TransactionCipherImpl): TransactionCipher

    @Binds
    abstract fun bindChronologicalSync(iml: ChronologicalSyncImpl): ChronologicalSync

    @Singleton
    @Binds
    abstract fun bindSyncCryptoChanger(impl: SyncCryptoChangerImpl): SyncCryptoChanger

    @Binds
    abstract fun bindRemoteKeyIdGenerator(impl: RemoteKeyIdGeneratorImpl): RemoteKeyIdGenerator
}