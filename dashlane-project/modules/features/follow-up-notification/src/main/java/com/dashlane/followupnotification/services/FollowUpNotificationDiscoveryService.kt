package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.data.FollowUpNotificationRepository
import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
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
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val followUpNotificationRepository: FollowUpNotificationRepository
) : FollowUpNotificationDiscoveryService {

    private val supportedTypes = FollowUpNotificationsTypes.values().map { it.syncObjectType }

    override fun canDisplayDiscoveryScreen(syncObjectType: SyncObjectType): Boolean {
        val canUseFeature = followUpNotificationFlags.canUseFollowUpNotification()
        val hasNotSeenDiscoveryScreen = !preferencesManager[sessionManager.session?.username].hasSeenFollowUpNotificationDiscoveryScreen
        val isVaultItemEligible = syncObjectType in supportedTypes

        return canUseFeature && hasNotSeenDiscoveryScreen && isVaultItemEligible
    }

    override fun setHasSeenIntroductionScreen() {
        preferencesManager[sessionManager.session?.username].hasSeenFollowUpNotificationDiscoveryScreen = true
    }

    override fun canDisplayReminderScreen(itemId: String?): Boolean {
        
        if (!followUpNotificationFlags.canUseFollowUpNotification()) {
            return false
        }
        if (preferencesManager[sessionManager.session?.username].hasAcknowledgedFollowUpNotificationReminderScreen) {
            return false
        }
        
        val (lastItemId, lastNotificationId, hasInteracted) = preferencesManager[sessionManager.session?.username].lastFollowUpNotificationItem
        if (itemId != lastItemId || followUpNotificationRepository.get(lastNotificationId) == null) {
            return false
        }
        
        if (hasInteracted) {
            return false
        }
        
        return true
    }

    override fun setHasAcknowledgedReminderScreen() {
        preferencesManager[sessionManager.session?.username].hasAcknowledgedFollowUpNotificationReminderScreen = true
    }

    override fun updateLastNotificationItem(itemId: String, notificationId: String, hasInteracted: Boolean) {
        preferencesManager[sessionManager.session?.username].lastFollowUpNotificationItem = Triple(itemId, notificationId, hasInteracted)
    }
}