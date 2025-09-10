package com.dashlane.search

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.SearchVaultItem
import jakarta.inject.Inject

class SearchLogger @Inject constructor(private val hermesLogRepository: LogRepository) {
    fun logClose(typedCharCount: Int, resultCount: Int) {
        hermesLogRepository.queueEvent(
            SearchVaultItem(
                totalCount = resultCount,
                hasInteracted = false,
                charactersTypedCount = typedCharCount,
            ),
        )
    }

    fun logClick(typedCharCount: Int, resultCount: Int) {
        hermesLogRepository.queueEvent(
            SearchVaultItem(
                totalCount = resultCount,
                hasInteracted = true,
                charactersTypedCount = typedCharCount,
            ),
        )
    }
}