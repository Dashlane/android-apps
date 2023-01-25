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



@Module
internal abstract class SyncBindingModule {

    @SyncScope
    @Binds
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    abstract fun bindSyncServices(impl: SyncServicesImpl): SyncServices

    @SyncScope
    @Binds
    abstract fun bindTreatProblemManager(impl: TreatProblemManagerImpl): TreatProblemManager

    @Binds
    abstract fun bindDeduplication(impl: SyncDeduplicationImpl): SyncDeduplication

    @SyncScope
    @Binds
    abstract fun bindSyncLogs(impl: SyncLogsImpl): SyncLogs

    @SyncScope
    @Binds
    abstract fun bindSyncMerger(impl: SyncMergerImpl): SyncMerger

    @Binds
    abstract fun bindTransactionCipher(impl: TransactionCipherImpl): TransactionCipher

    @Binds
    abstract fun bindChronologicalSync(iml: ChronologicalSyncImpl): ChronologicalSync

    @SyncScope
    @Binds
    abstract fun bindSyncCryptoChanger(impl: SyncCryptoChangerImpl): SyncCryptoChanger

    @Binds
    abstract fun bindRemoteKeyIdGenerator(impl: RemoteKeyIdGeneratorImpl): RemoteKeyIdGenerator
}