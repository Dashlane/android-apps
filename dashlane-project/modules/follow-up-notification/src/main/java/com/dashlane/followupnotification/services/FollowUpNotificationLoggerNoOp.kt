package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.util.clipboard.vault.CopyField
import javax.inject.Inject

class FollowUpNotificationLoggerNoOp @Inject constructor() : FollowUpNotificationLogger {
    override fun showFollowUp(followUpType: FollowUpNotificationsTypes, copyField: CopyField?) = Unit
    override fun dismissFollowUp(followUpType: FollowUpNotificationsTypes) = Unit
    override fun copyFieldFromFollowUp(
        followUpType: FollowUpNotificationsTypes,
        copiedField: CopyField,
        itemId: String,
        isProtected: Boolean,
        domain: String?
    ) = Unit

    override fun logDisplayDiscoveryIntroduction() = Unit
    override fun logDisplayDiscoveryReminder() = Unit
    override fun logDeactivateFollowUpNotification() = Unit
    override fun logActivateFollowUpNotification() = Unit
}