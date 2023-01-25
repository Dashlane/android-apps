package com.dashlane.teamspaces.manager

import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode108



class TeamspaceSettingsLogger(
    private val usageLogRepository: UsageLogRepository,
    private val teamspaceManager: TeamspaceManager,
    private val userPreferencesManager: UserPreferencesManager
) {

    fun sendSettingsLog108() {
        teamspaceManager.all
            .filterNot { it === TeamspaceManager.COMBINED_TEAMSPACE || it === TeamspaceManager.PERSONAL_TEAMSPACE }
            .forEach { space ->
                sendLogAutoLock(space)
                sendLogEmergency(space)
                sendLogSharing(space)
                sendLogForcedCategorization(space)
                sendLogSmartCategorization(space)
                sendLogRevoked(space)
            }
        userPreferencesManager.putLong(ConstantsPrefs.LAST_UL108_SENT, System.currentTimeMillis())
    }

    @VisibleForTesting
    fun sendLogSharing(space: Teamspace) {
        sendSettingEnabled(
            space.anonTeamId, UsageLogCode108.Type.SHARING,
            !space.featureDisabledForSpace(Teamspace.Feature.SHARING_DISABLED)
        )
    }

    @VisibleForTesting
    fun sendLogEmergency(space: Teamspace) {
        sendSettingEnabled(
            space.anonTeamId, UsageLogCode108.Type.EMERGENCY,
            !space.featureDisabledForSpace(Teamspace.Feature.EMERGENCY_DISABLED)
        )
    }

    @VisibleForTesting
    fun sendLogAutoLock(space: Teamspace) {
        sendSettingEnabled(
            space.anonTeamId, UsageLogCode108.Type.LOCK_ON_EXIT,
            space.featureDisabledForSpace(Teamspace.Feature.AUTOLOCK)
        )
    }

    @VisibleForTesting
    fun sendLogForcedCategorization(space: Teamspace) {
        sendSettingEnabled(
            space.anonTeamId, UsageLogCode108.Type.FORCED_CATEGORIZATION,
            space.isDomainRestrictionsEnable
        )
    }

    @VisibleForTesting
    fun sendLogRevoked(space: Teamspace) { 
        sendSettingEnabled(
            space.anonTeamId, UsageLogCode108.Type.REMOVE_REVOKED,
            space.isRemoveForcedContentEnabled
        )
    }

    @VisibleForTesting
    fun sendLogSmartCategorization(space: Teamspace) {
        val numDomains = space.domains.size
        if (numDomains > 0) {
            usageLogRepository.enqueue(
                UsageLogCode108(
                    spaceId = space.anonTeamId,
                    type = UsageLogCode108.Type.SMART_CATEGORIZATION,
                    value = numDomains.toString()
                )
            )
        }
    }

    private fun sendSettingEnabled(
        anonTeamId: String,
        type: UsageLogCode108.Type,
        enabled: Boolean
    ) {
        usageLogRepository.enqueue(
            UsageLogCode108(
                spaceId = anonTeamId,
                type = type,
                value = if (enabled) "enabled" else "disabled"
            )
        )
    }

    companion object {

        @JvmStatic
        fun logIfNeeded() {
            if (shouldLogSettings()) {
                val session = SingletonProvider.getSessionManager().session
                    ?: return 
                val component = SingletonProvider.getComponent()
                val usageLogRepository = component.bySessionUsageLogRepository[session]
                    ?: return 
                val teamspaceManager = component.teamspaceRepository.getTeamspaceManager(session)
                    ?: return 

                TeamspaceSettingsLogger(usageLogRepository, teamspaceManager, component.userPreferencesManager)
                    .sendSettingsLog108()
            }
        }

        private fun shouldLogSettings(): Boolean {
            val preferencesManager = SingletonProvider.getUserPreferencesManager()
            if (preferencesManager.exist(ConstantsPrefs.LAST_UL108_SENT)) {
                val lastTimestamp = preferencesManager.getLong(ConstantsPrefs.LAST_UL108_SENT)
                return System.currentTimeMillis() - lastTimestamp > DateUtils.WEEK_IN_MILLIS
            }
            return true
        }
    }
}