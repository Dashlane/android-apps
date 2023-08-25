package com.dashlane.autofill.core

import com.dashlane.autofill.api.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.api.util.DomainWrapper
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
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.copyWithValuesFromAuthentifiant
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
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
    }
}