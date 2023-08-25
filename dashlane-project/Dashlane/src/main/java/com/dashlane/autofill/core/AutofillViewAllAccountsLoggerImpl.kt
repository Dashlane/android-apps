package com.dashlane.autofill.core

import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.api.viewallaccounts.AutofillViewAllAccountsLogger
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.SearchVaultItem
import com.dashlane.hermes.generated.events.user.SelectVaultItem
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillViewAllAccountsLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository
) : AutofillViewAllAccountsLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun onResultsLoaded() {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_EXPLORE_PASSWORDS_SEARCH
        )
    }

    override fun onSelectFromViewAllAccount(
        @AutofillOrigin origin: Int,
        packageName: String,
        webappDomain: String,
        itemUrl: String?,
        itemId: String?,
        itemListContext: ItemListContext?
    ) {
        itemId?.let {
            logRepository.queueEvent(
                SelectVaultItem(
                    highlight = Highlight.SEARCH_RESULT,
                    itemId = ItemId(id = it),
                    itemType = ItemType.CREDENTIAL,
                    totalCount = itemListContext?.sectionCount,
                    index = itemListContext?.positionInContainerSection?.toDouble()
                )
            )
        }
    }

    override fun onViewAllAccountOver(
        totalCount: Int,
        hasInteracted: Boolean,
        charactersTypedCount: Int
    ) {
        if (hasInteracted) {
            logRepository.queueEvent(
                SearchVaultItem(
                    totalCount = totalCount,
                    hasInteracted = hasInteracted,
                    charactersTypedCount = charactersTypedCount
                )
            )
        } else {
            logRepository.queueEvent(
                AutofillDismiss(
                    dismissType = DismissType.CLOSE
                )
            )
        }
    }
}