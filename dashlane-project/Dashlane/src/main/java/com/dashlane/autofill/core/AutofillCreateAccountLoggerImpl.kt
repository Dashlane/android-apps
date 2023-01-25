package com.dashlane.autofill.core

import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.api.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.api.util.DomainWrapper
import com.dashlane.autofill.core.AutofillUsageLog.Companion.getSenderUsage96
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.anonymous.AutofillAcceptAnonymous
import com.dashlane.hermes.generated.events.anonymous.AutofillDismissAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode57
import com.dashlane.useractivity.log.usage.UsageLogCode96
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.copyWithValuesFromAuthentifiant
import com.dashlane.useractivity.log.usage.fillAndSend
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.vault.util.getTeamSpaceLog
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class AutofillCreateAccountLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository
) : AutofillCreateAccountLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun onCancel(
        domainWrapper: DomainWrapper
    ) {
        val dismissType = DismissType.CLOSE

        logRepository.queueEvent(
            AutofillDismiss(
                dismissType = dismissType
            )
        )
        logRepository.queueEvent(
            AutofillDismissAnonymous(
                dismissType = dismissType,
                domain = domainWrapper.domain,
                isNativeApp = domainWrapper.isNativeApp
            )
        )
    }

    override fun logSave(
        domainWrapper: DomainWrapper,
        credential: VaultItem<SyncObject.Authentifiant>
    ) {
        val dataType = listOf(ItemType.CREDENTIAL)
        logRepository.queueEvent(
            AutofillAccept(dataTypeList = dataType)
        )
        logRepository.queueEvent(
            AutofillAcceptAnonymous(domain = domainWrapper.domain)
        )
        logRepository.queueEvent(
            UpdateVaultItem(
                itemId = ItemId(credential.uid),
                itemType = ItemType.CREDENTIAL,
                action = Action.ADD,
                space = credential.getTeamSpaceLog(),
            )
        )

        
        val session = sessionManager.session ?: return
        val teamId = tryOrNull {
            teamspaceRepository.getTeamspaceManager(session)
                ?.get(TeamSpaceUtils.getTeamSpaceId(credential))?.anonTeamId
        }

        log(
            UsageLogCode11(
                spaceId = teamId,
                type = UsageLogCode11.Type.AUTHENTICATION,
                from = UsageLogCode11.From.AUTOFILL_CREATE_ACCOUNT,
                action = UsageLogCode11.Action.ADD
            ).copyWithValuesFromAuthentifiant(credential)
        )

        bySessionUsageLogRepository[sessionManager.session]?.apply {
            UsageLogCode57(
                spaceId = teamId,
                sender = UsageLogCode57.Sender.AUTOFILL_CREATE_ACCOUNT,
                action = UsageLogCode57.Action.ADD
            ).fillAndSend(this, credential.toSummary())
        }
    }

    override fun logOnClickCreateAccount(
        @AutofillOrigin origin: Int,
        packageName: String,
        webAppDomain: String,
        hasCredential: Boolean
    ) {
        log(
            UsageLogCode96(
                action = UsageLogCode96.Action.CLICK_CREATE_ACCOUNT,
                app = packageName,
                sender = getSenderUsage96(origin),
                webappDomain = webAppDomain,
                hasCredentials = hasCredential,
                type = UsageLogCode96.Type.AUTHENTICATION
            )
        )
    }
}