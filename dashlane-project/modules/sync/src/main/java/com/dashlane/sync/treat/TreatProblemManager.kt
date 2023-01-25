package com.dashlane.sync.treat

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.sync.repositories.ServerCredentials
import com.dashlane.sync.repositories.SyncRepository
import com.dashlane.sync.vault.SyncVault



interface TreatProblemManager {

    

    suspend fun execute(
        serverCredentials: ServerCredentials,
        serverSummary: List<SyncSummaryItem>,
        syncVault: SyncVault,
        cryptographyEngineFactory: CryptographyEngineFactory
    ): Result

    data class Result(
        val type: SyncRepository.Result.TreatProblemType,
        val transactionErrors: List<Throwable>
    )
}
