package com.dashlane.ui.activities.fragments.list.action

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode114
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.getUsageLogNameFromType
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject



class ActionItemLogger @Inject constructor(
    val sessionManager: SessionManager,
    val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    fun sendOpenFromSearchMostSearchedLog(subAction: String) {
        sendOpenItemLog(
            type = ORIGIN_SEARCH_MOST_SEARCHED,
            action = ACTION_PICK,
            subAction = subAction
        )
    }

    fun sendOpenFromSearchResultLog() {
        sendOpenItemLog(
            type = ORIGIN_SEARCH_RESULTS,
            action = ACTION_PICK
        )
    }

    private fun sendOpenItemLog(
        type: String?,
        subType: String? = null,
        action: String?,
        subAction: String? = null,
        website: String? = null,
        position: Int? = null
    ) {
        log(
            UsageLogCode75(
                type = type,
                subtype = subType,
                action = action,
                subaction = subAction,
                website = website,
                position = position
            )
        )
    }

    fun sendMainContentCopiedLog(item: SummaryObject, itemListContext: ItemListContext) {
        
        val origin: String = itemListContext.container.label + itemListContext.section.label
        val position = itemListContext.positionInContainerSection.toLong()
        val field = when (item) {
            is SummaryObject.Authentifiant -> "password"
            else -> null
        }

        val website = (item as? SummaryObject.Authentifiant)?.navigationUrl?.toUrlOrNull()?.root

        sendUsageLogCode114(
            origin = origin,
            action = ACTION_COPY,
            vaultItem = item,
            field = field,
            website = website,
            position = position
        )
    }

    private fun sendUsageLogCode114(
        origin: String,
        action: String,
        vaultItem: SummaryObject,
        field: String? = null,
        website: String? = null,
        position: Long
    ) {
        log(
            UsageLogCode114(
                action = action,
                itemId = vaultItem.anonymousId,
                itemType = vaultItem.getUsageLog114ItemType(),
                field = field,
                sender = origin,
                website = website,
                position = position
            )
        )
    }

    private fun SummaryObject.getUsageLog114ItemType(): UsageLogCode114.ItemType? {
        return getUsageLogNameFromType(syncObjectType)?.code
            ?.let { code -> UsageLogCode114.ItemType.values().firstOrNull { it.code == code } }
    }

    fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }

    companion object {
        const val ORIGIN_SEARCH_RESULTS = "searchResults"
        const val ORIGIN_SEARCH_MOST_SEARCHED = "mostSearchedItems"

        const val ACTION_PICK = "pick"

        const val ACTION_COPY = "copy"

        @JvmStatic
        fun create(): ActionItemLogger {
            return ActionItemLogger(
                SingletonProvider.getSessionManager(),
                SingletonProvider.getComponent().bySessionUsageLogRepository
            )
        }
    }
}