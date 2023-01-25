package com.dashlane.ui.activities.fragments.vault

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class VaultLogger @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager
) : Vault.Logger {
    override fun onListDisplayed(filter: Filter) {
        log75(filter.logTag, DISPLAY)
    }

    override fun logClickOpenItem(itemListContext: ItemListContext, website: String?, filter: Filter) {
        val action = CLICK + itemListContext.section.label
        val subaction = "${CLICKED}_${filter.logTag}"
        val position = itemListContext.positionInContainerSection
        log(
            UsageLogCode75(
                type = TYPE_DASHBOARD,
                subtype = SUBTYPE_VAULT,
                action = action,
                subaction = subaction,
                website = website,
                position = position
            )
        )
    }

    override fun onFilterSelected(filter: Filter) {
        log75("${FILTER}_${filter.logTag}", CLICKED)
    }

    override fun buttonSearchClicked() {
        log75("searchbar", CLICKED)
    }

    override fun logAnnouncement(action: String, type: String) {
        log(UsageLogCode75(type = type, action = action, originStr = SUBTYPE_VAULT))
    }

    override fun logHiddenImpala() {
        log75("hiddenimpala", DISPLAY)
    }

    private fun log75(action: String, subaction: String? = null) {
        log(UsageLogCode75(TYPE_DASHBOARD, SUBTYPE_VAULT, action, subaction))
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }

    companion object {
        private const val TYPE_DASHBOARD = "dashboard"
        private const val SUBTYPE_VAULT = "vault"
        private const val CLICK = "click"
        private const val CLICKED = "clicked"
        private const val DISPLAY = "display"
        private const val FILTER = "filter"
    }
}