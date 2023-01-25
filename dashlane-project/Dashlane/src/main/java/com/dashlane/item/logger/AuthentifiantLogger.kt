package com.dashlane.item.logger

import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogCode57
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.fillAndSend
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

class AuthentifiantLogger(
    private val teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    private val sender: UsageLogCode57.Sender,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    @Suppress("UNCHECKED_CAST")
    override fun logItemAdded(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemAdded(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.Authentifiant) return
        sendUsageLog57(vaultItem as VaultItem<SyncObject.Authentifiant>, UsageLogCode57.Action.ADD, sender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun logItemModified(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemModified(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.Authentifiant) return
        sendUsageLog57(vaultItem as VaultItem<SyncObject.Authentifiant>, UsageLogCode57.Action.EDIT, sender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun logItemDeleted(vaultItem: VaultItem<*>, dataType: SyncObjectType) {
        super.logItemDeleted(vaultItem, dataType)
        if (vaultItem.syncObject !is SyncObject.Authentifiant) return
        sendUsageLog57(vaultItem as VaultItem<SyncObject.Authentifiant>, UsageLogCode57.Action.REMOVE, sender)
    }

    fun logCopyLogin(url: String) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.COPY_LOGIN,
                website = url
            )
        )
    }

    fun logCopyPassword(url: String) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.COPY_PASSWORD,
                website = url
            )
        )
    }

    fun logRevealPassword(url: String) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.SHOW_PASSWORD,
                website = url
            )
        )
    }

    fun logGoToWebsiteOptions() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogConstant.ActionType.askGotoBrowserAfterCopyPassword
            )
        )
    }

    fun logGoToWebsite(isPasswordCopied: Boolean, isLoginCopied: Boolean, packageName: String) {
        val action = when {
            isPasswordCopied -> UsageLogConstant.ActionType.gotoBrowserAfterCopyPassword
            isLoginCopied -> UsageLogConstant.ActionType.gotoBrowserAfterCopyLogin
            else -> UsageLogConstant.ActionType.gotoBrowser
        }
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = action,
                subaction = packageName
            )
        )
    }

    fun logExpandLinkedDomains(vaultItemUrlDomain: String, list: List<String>) {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.AUTHENTICATION.code,
                action = UsageLogCode35Action.EXPAND_ASSOCIATED_DOMAINS,
                nbOfItems = list.size,
                website = vaultItemUrlDomain
            )
        )
    }

    private fun sendUsageLog57(
        item: VaultItem<SyncObject.Authentifiant>,
        action: UsageLogCode57.Action,
        sender: UsageLogCode57.Sender
    ) {
        val usageLogRepository = usageLogRepository ?: return
        UsageLogCode57(
            spaceId = teamspaceAccessor.get(TeamSpaceUtils.getTeamSpaceId(item))?.anonTeamId,
            action = action,
            sender = sender
        ).fillAndSend(usageLogRepository, item.toSummary())
    }
}