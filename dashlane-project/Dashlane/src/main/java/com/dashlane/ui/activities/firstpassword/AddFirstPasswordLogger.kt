package com.dashlane.ui.activities.firstpassword

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogCode57
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.copyWithValuesFromAuthentifiant
import com.dashlane.useractivity.log.usage.fillAndSend
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class AddFirstPasswordLogger @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository
) : AddFirstPassword.Logger {

    private val logger: UsageLogRepository?
        get() = bySessionUsageLogRepository[sessionManager.session]

    override fun display() {
        log(ACTION_DISPLAY, "page_add_password")
    }

    override fun onClickSecureButton() {
        log(ACTION_CLICK, "secure_faq")
    }

    override fun onClickSaveButton() {
        log(ACTION_CLICK, "save_password")
    }

    override fun onClickTryDemo() {
        log(ACTION_CLICK, "try_autofill_demo")
    }

    override fun onClickReturnHome() {
        log(ACTION_CLICK, "return_home")
    }

    override fun onCredentialSaved(credential: VaultItem<SyncObject.Authentifiant>) {
        val teamId = tryOrNull {
            teamspaceRepository.getTeamspaceManager(sessionManager.session!!)
                ?.get(TeamSpaceUtils.getTeamSpaceId(credential))?.anonTeamId
        }

        logger?.apply {
            enqueue(
                UsageLogCode11(
                    spaceId = teamId,
                    type = UsageLogCode11.Type.AUTHENTICATION,
                    from = UsageLogCode11.From.AUTOFILL_DEMO,
                    action = UsageLogCode11.Action.ADD
                ).copyWithValuesFromAuthentifiant(credential)
            )
            UsageLogCode57(
                spaceId = teamId,
                sender = UsageLogCode57.Sender.AUTOFILL_DEMO,
                action = UsageLogCode57.Action.ADD
            ).fillAndSend(this, credential.toSummary())
        }
    }

    private fun log(action: String, subAction: String? = null) {
        logger?.enqueue(
            UsageLogCode35(
                type = "autofill_demo",
                action = action,
                subaction = subAction
            )
        )
    }

    companion object {
        private const val ACTION_DISPLAY = "display"
        private const val ACTION_CLICK = "click"
    }
}