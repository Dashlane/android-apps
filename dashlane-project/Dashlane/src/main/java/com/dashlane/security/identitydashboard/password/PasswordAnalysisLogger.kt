package com.dashlane.security.identitydashboard.password

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode125
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.inject.OptionalProvider
import kotlin.math.roundToInt



class PasswordAnalysisLogger(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private var teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private var freshStart: Boolean
) {

    var nextLogIsUpdate = false
    var origin: String? = null

    private var lastMode: PasswordAnalysisContract.Mode? = null
    private var lastSensitiveAccountOnly: Boolean = false

    fun onViewVisible() {
        nextLogIsUpdate = !freshStart
        if (freshStart) {
            freshStart = false
        }
    }

    fun onPageSelected(mode: PasswordAnalysisContract.Mode, sensitiveAccountOnly: Boolean, securityScore: Float) {
        sendUsageLog125(mode, sensitiveAccountOnly, getActionForView(), securityScore = securityScore)
    }

    fun onAction(action: UsageLogCode125.Action, itemWebsite: String?) {
        val mode = lastMode ?: return
        sendUsageLog125(mode, lastSensitiveAccountOnly, action, itemWebsite = itemWebsite)
    }

    private fun sendUsageLog125(
        mode: PasswordAnalysisContract.Mode,
        sensitiveAccountOnly: Boolean,
        action: UsageLogCode125.Action,
        securityScore: Float = -1F,
        itemWebsite: String? = null
    ) {
        lastMode = mode
        lastSensitiveAccountOnly = sensitiveAccountOnly

        val subType: UsageLogCode125.TypeSub? = getSubType(mode, sensitiveAccountOnly) ?: return

        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode125(
                    type = UsageLogCode125.Type.PASSWORD_HEALTH,
                    typeSub = subType,
                    action = action,
                    securityScore = (securityScore * 100).roundToInt().takeIf { it > 0 },
                    origin = origin,
                    spaceId = teamspaceAccessorProvider.get()?.current?.anonTeamId,
                    website = itemWebsite
                )
            )

        origin = null 
    }

    private fun getActionForView(): UsageLogCode125.Action {
        val action = if (nextLogIsUpdate) {
            UsageLogCode125.Action.UPDATE
        } else {
            UsageLogCode125.Action.SHOW
        }
        nextLogIsUpdate = false
        return action
    }

    private fun getSubType(
        mode: PasswordAnalysisContract.Mode,
        sensitiveAccountOnly: Boolean
    ): UsageLogCode125.TypeSub? {
        return when {
            mode == PasswordAnalysisContract.Mode.COMPROMISED && sensitiveAccountOnly ->
                UsageLogCode125.TypeSub.COMPROMISE_SENSITIVE
            mode == PasswordAnalysisContract.Mode.COMPROMISED ->
                UsageLogCode125.TypeSub.COMPROMISE_ALL
            mode == PasswordAnalysisContract.Mode.REUSED && sensitiveAccountOnly ->
                UsageLogCode125.TypeSub.REUSED_SENSITIVE
            mode == PasswordAnalysisContract.Mode.REUSED ->
                UsageLogCode125.TypeSub.REUSED_ALL
            mode == PasswordAnalysisContract.Mode.WEAK && sensitiveAccountOnly ->
                UsageLogCode125.TypeSub.WEAK_SENSITIVE
            mode == PasswordAnalysisContract.Mode.WEAK ->
                UsageLogCode125.TypeSub.WEAK_ALL
            mode == PasswordAnalysisContract.Mode.EXCLUDED && sensitiveAccountOnly ->
                UsageLogCode125.TypeSub.EXCLUDED_SENSITIVE
            mode == PasswordAnalysisContract.Mode.EXCLUDED ->
                UsageLogCode125.TypeSub.EXCLUDED_ALL
            else -> null
        }
    }
}