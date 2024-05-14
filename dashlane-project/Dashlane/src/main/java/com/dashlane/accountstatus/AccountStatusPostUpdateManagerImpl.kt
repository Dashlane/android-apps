package com.dashlane.accountstatus

import com.dashlane.accountstatus.premiumstatus.AccountStatusPreferencesRefresher
import com.dashlane.accountstatus.premiumstatus.DeviceNumberUpdater
import com.dashlane.teamspaces.db.SmartSpaceCategorizationManager
import com.dashlane.teamspaces.manager.RevokedDetector
import dagger.Lazy
import javax.inject.Inject

class AccountStatusPostUpdateManagerImpl @Inject constructor(
    private val smartSpaceCategorizationManager: Lazy<SmartSpaceCategorizationManager>,
    private val revokedDetector: RevokedDetector,
    private val preferencesRefresh: AccountStatusPreferencesRefresher,
    private val deviceNumberUpdater: DeviceNumberUpdater,
) : AccountStatusPostUpdateManager {

    override suspend fun onUpdate(newStatus: AccountStatus, oldStatus: AccountStatus?) {
        
        smartSpaceCategorizationManager.get().executeSync()

        
        revokedDetector.onStatusChanged(newStatus = newStatus, oldStatus = oldStatus)

        
        preferencesRefresh.resetPremiumNotificationIfChanged(newStatus = newStatus.premiumStatus, lastSaved = oldStatus?.premiumStatus)

        
        deviceNumberUpdater.updateNumberOfDevices()
    }
}