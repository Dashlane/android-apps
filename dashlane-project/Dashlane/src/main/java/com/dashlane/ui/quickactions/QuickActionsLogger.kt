package com.dashlane.ui.quickactions

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.DropdownType
import com.dashlane.hermes.generated.events.user.OpenVaultItemDropdown
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.toHighlight
import com.dashlane.vault.toItemType
import javax.inject.Inject

class QuickActionsLogger @Inject constructor(private val logRepository: LogRepository) : QuickActionsContract.Logger {
    override fun logOpenQuickActions(
        summaryObject: SummaryObject,
        itemListContext: ItemListContext
    ) {
        logRepository.queuePageView(
            component = BrowseComponent.MAIN_APP,
            page = AnyPage.ITEM_QUICK_ACTIONS_DROPDOWN
        )
        logRepository.queueEvent(
            OpenVaultItemDropdown(
                highlight = itemListContext.section.toHighlight(),
                dropdownType = DropdownType.QUICK_ACTIONS,
                itemType = summaryObject.syncObjectType.toItemType(),
                totalCount = itemListContext.sectionCount,
                index = itemListContext.positionInContainerSection.toDouble()
            )
        )
    }

    override fun logCloseQuickActions(originPage: String?) {
        val page = AnyPage.values().firstOrNull { it.code == originPage } ?: return

        logRepository.queuePageView(
            component = BrowseComponent.MAIN_APP,
            page = page
        )
    }
}