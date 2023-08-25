package com.dashlane.sync.domain

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction
import com.dashlane.server.api.endpoints.sync.SyncUploadTransaction
import kotlinx.coroutines.channels.SendChannel

interface TransactionCipher {

    suspend fun decipherIncomingTransactions(
        transactions: List<SyncDownloadTransaction>,
        cryptographyEngineFactory: CryptographyEngineFactory,
        sharedIds: Set<String>,
        progressChannel: SendChannel<Unit>? = null
    ): Pair<List<IncomingTransaction>, List<Throwable>>

    suspend fun cipherOutgoingTransactions(
        transactions: List<OutgoingTransaction>,
        cryptographyEngineFactory: CryptographyEngineFactory
    ): List<SyncUploadTransaction>
}