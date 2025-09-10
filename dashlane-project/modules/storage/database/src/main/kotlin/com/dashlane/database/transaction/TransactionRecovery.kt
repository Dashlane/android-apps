package com.dashlane.database.transaction

import com.dashlane.database.SearchRepositoryImpl
import com.dashlane.database.SharingRepositoryImpl
import com.dashlane.database.TransactionRepository
import com.dashlane.database.TransactionRepositoryImpl
import com.dashlane.database.TransactionSearchRepositoryImpl
import com.dashlane.database.TransactionSharingRepositoryImpl
import com.dashlane.database.VaultObjectRepositoryImpl

internal class TransactionRecovery(
    private val transactionRepository: TransactionRepositoryImpl,
    private val transactionSharingRepository: TransactionSharingRepositoryImpl,
    private val transactionSearchRepository: TransactionSearchRepositoryImpl,
    private val vaultObjectRepository: VaultObjectRepositoryImpl,
    private val sharingRepository: SharingRepositoryImpl,
    private val searchRepository: SearchRepositoryImpl,
) {
    suspend fun recovery() {
        recovery(vaultObjectRepository, transactionRepository)
        recovery(sharingRepository, transactionSharingRepository)
        recovery(searchRepository, transactionSearchRepository)
    }

    private suspend fun recovery(
        repository: RecoverableRepository,
        transactionRepository: TransactionRepository,
    ) {
        
        if (!transactionRepository.markerExist) {
            transactionRepository.releaseMarker()
            return
        }
        repository.applyPendingTransaction()
        transactionRepository.releaseMarker()
    }
}