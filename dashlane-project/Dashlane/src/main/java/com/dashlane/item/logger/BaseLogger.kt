package com.dashlane.item.logger

import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode34
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.getUsageLogNameFromType
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getUsageLogLabel
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.model.toSql
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.vault.util.attachmentsCount
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

open class BaseLogger(
    private val teamspaceAccessor: TeamspaceAccessor,
    private val dataCounter: DataCounter,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : Logger {

    val usageLogRepository: UsageLogRepository?
        get() = bySessionUsageLogRepository[sessionManager.session]

    override fun log(log: UsageLog) {
        usageLogRepository?.enqueue(log)
    }

    override fun logDisplay(dataType: SyncObjectType) {
        val logName = dataType.toSql()?.tableName ?: return
        log(
            UsageLogCode34(
                spaceId = teamspaceAccessor.current?.anonTeamId,
                viewName = "ul-$logName"
            )
        )
    }

    override fun logItemAdded(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        sendUsageLog11(vaultItem, dataType, UsageLogCode11.Action.ADD, categorizationMethod)
    }

    override fun logItemModified(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        sendUsageLog11(vaultItem, dataType, UsageLogCode11.Action.EDIT, categorizationMethod)
    }

    override fun logItemDeleted(vaultItem: VaultItem<*>, dataType: SyncObjectType) {
        sendUsageLog11(vaultItem, dataType, UsageLogCode11.Action.REMOVE, null)
    }

    private fun sendUsageLog11(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        action: UsageLogCode11.Action,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        val item = vaultItem.syncObject
        val summaryObject: SummaryObject = vaultItem.toSummary()
        val spaceId = if (vaultItem.isSpaceItem()) {
            teamspaceAccessor.get(TeamSpaceUtils.getTeamSpaceId(vaultItem))?.anonTeamId
        } else null
        val from = when (categorizationMethod) {
            ItemEditSpaceSubView.CategorizationMethod.FORCED_CATEGORIZATION -> UsageLogCode11.From.FORCED_CATEGORIZATION
            ItemEditSpaceSubView.CategorizationMethod.SMART_CATEGORIZATION -> UsageLogCode11.From.SMART_CATEGORIZATION
            else -> UsageLogCode11.From.MANUAL
        }
        log(
            UsageLogCode11(
                itemId = vaultItem.anonymousId,
                type = getUsageLogNameFromType(dataType),
                counter = dataCounter.count(CounterFilter(SpecificDataTypeFilter(dataType))),
                country = item.localeFormat?.isoCode,
                documentCount = summaryObject.attachmentsCount().toLong(),
                action = action,
                from = from,
                spaceId = spaceId
            ).let {
                when (item) {
                    is SyncObject.Authentifiant -> it.copy(
                        website = item.urlForUsageLog,
                        category = item.category
                    )
                    is SyncObject.SecureNote -> it.copy(
                        color = item.type?.getUsageLogLabel(),
                        size = item.content?.length?.toLong() ?: 0L,
                        secure = item.secured ?: false,
                        category = item.category
                    )
                    is SyncObject.PaymentCreditCard -> it.copy(
                        details = (this as CreditCardLogger).origin
                    )
                    else -> it
                }
            }
        )
    }
}