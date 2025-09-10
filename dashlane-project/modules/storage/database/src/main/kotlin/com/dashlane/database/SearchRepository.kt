package com.dashlane.database

import com.dashlane.database.model.DatabaseSearch
import com.dashlane.database.model.SearchItem

interface SearchRepository {
    suspend fun transaction(
        timeoutMillis: Long = VaultObjectRepository.DEFAULT_TIMEOUT_MILLIS,
        block: suspend Transaction.() -> Unit,
    )

    suspend fun load(): DatabaseSearch

    interface Transaction {
        fun update(item: SearchItem)

        fun delete(ids: Set<String>)
    }
}