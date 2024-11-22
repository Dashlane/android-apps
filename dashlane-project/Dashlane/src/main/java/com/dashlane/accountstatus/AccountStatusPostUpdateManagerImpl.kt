package com.dashlane.accountstatus

import com.dashlane.accountstatus.premiumstatus.DeviceNumberUpdater
import com.dashlane.accountstatus.premiumstatus.endDate
import com.dashlane.accountstatus.premiumstatus.familyStatus
import com.dashlane.accountstatus.premiumstatus.planFeature
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.teamspaces.db.SmartSpaceCategorizationManager
import com.dashlane.teamspaces.manager.RevokedDetector
import com.dashlane.user.Username
import dagger.Lazy
import javax.inject.Inject

class AccountStatusPostUpdateManagerImpl @Inject constructor(
    private val smartSpaceCategorizationManager: Lazy<SmartSpaceCategorizationManager>,
    private val revokedDetector: RevokedDetector,
    private val preferencesManager: PreferencesManager,
    private val deviceNumberUpdater: DeviceNumberUpdater,
) : AccountStatusPostUpdateManager {

    override suspend fun onUpdate(username: Username, newStatus: AccountStatus, oldStatus: AccountStatus?) {
        
        smartSpaceCategorizationManager.get().executeSync()

        
        revokedDetector.onStatusChanged(newStatus = newStatus, oldStatus = oldStatus)

        
        resetPremiumNotificationIfChanged(username = username, newStatus = newStatus.premiumStatus, lastSaved = oldStatus?.premiumStatus)

        
        deviceNumberUpdater.updateNumberOfDevices()
    }

    private fun resetPremiumNotificationIfChanged(
        username: Username,
        newStatus: PremiumStatus,
        lastSaved: PremiumStatus?
    ) {
        lastSaved ?: return

        val hasPlanFeaturesChanged = newStatus.planFeature != lastSaved.planFeature
        val hasEndDateChanged = newStatus.endDate != lastSaved.endDate
        val hasFamilyStatusChanged = newStatus.familyStatus != lastSaved.familyStatus
        val deletePref = hasPlanFeaturesChanged || hasEndDateChanged || hasFamilyStatusChanged
        if (deletePref) {
            preferencesManager[username].apply(
                listOf(
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_FIRST_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_SECOND_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_THIRD_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.GRACE_PERIOD_END_NOTIFICATION_DONE
                    )
                )
            )
        }
    }
}