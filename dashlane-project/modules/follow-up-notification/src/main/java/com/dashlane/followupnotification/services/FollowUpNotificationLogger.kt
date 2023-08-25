package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.util.clipboard.vault.CopyField

interface FollowUpNotificationLogger {
    fun showFollowUp(followUpType: FollowUpNotificationsTypes, copyField: CopyField?)
    fun dismissFollowUp(followUpType: FollowUpNotificationsTypes)
    fun copyFieldFromFollowUp(
        followUpType: FollowUpNotificationsTypes,
        copiedField: CopyField,
        itemId: String,
        isProtected: Boolean,
        domain: String?
    )

    fun logDisplayDiscoveryIntroduction()
    fun logDisplayDiscoveryReminder()
    fun logDeactivateFollowUpNotification()
    fun logActivateFollowUpNotification()
}