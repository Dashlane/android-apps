package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.data.FollowUpNotificationRepository
import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

interface FollowUpNotificationDiscoveryService {
    fun canDisplayDiscoveryScreen(syncObjectType: SyncObjectType): Boolean
    fun setHasSeenIntroductionScreen()
    fun canDisplayReminderScreen(itemId: String?): Boolean
    fun setHasAcknowledgedReminderScreen()
    fun updateLastNotificationItem(itemId: String, notificationId: String, hasInteracted: Boolean)
}

class FollowUpNotificationDiscoveryServiceImpl @Inject constructor(
    private val followUpNotificationFlags: FollowUpNotificationFlags,
    private val preferencesManager: UserPreferencesManager,
    private val followUpNotificationRepository: FollowUpNotificationRepository
) : FollowUpNotificationDiscoveryService {

    private val supportedTypes = FollowUpNotificationsTypes.values().map { it.syncObjectType }

    override fun canDisplayDiscoveryScreen(syncObjectType: SyncObjectType): Boolean {
        val canUseFeature = followUpNotificationFlags.canUseFollowUpNotification()
        val hasNotSeenDiscoveryScreen = !preferencesManager.hasSeenFollowUpNotificationDiscoveryScreen
        val isVaultItemEligible = syncObjectType in supportedTypes

        return canUseFeature && hasNotSeenDiscoveryScreen && isVaultItemEligible
    }

    override fun setHasSeenIntroductionScreen() {
        preferencesManager.hasSeenFollowUpNotificationDiscoveryScreen = true
    }

    override fun canDisplayReminderScreen(itemId: String?): Boolean {
        
        if (!followUpNotificationFlags.canUseFollowUpNotification()) {
            return false
        }
        if (preferencesManager.hasAcknowledgedFollowUpNotificationReminderScreen) {
            return false
        }
        
        val (lastItemId, lastNotificationId, hasInteracted) = preferencesManager.lastFollowUpNotificationItem
        if (itemId != lastItemId || followUpNotificationRepository.get(lastNotificationId) == null) {
            return false
        }
        
        if (hasInteracted) {
            return false
        }
        
        return true
    }

    override fun setHasAcknowledgedReminderScreen() {
        preferencesManager.hasAcknowledgedFollowUpNotificationReminderScreen = true
    }

    override fun updateLastNotificationItem(itemId: String, notificationId: String, hasInteracted: Boolean) {
        preferencesManager.lastFollowUpNotificationItem = Triple(itemId, notificationId, hasInteracted)
    }
}