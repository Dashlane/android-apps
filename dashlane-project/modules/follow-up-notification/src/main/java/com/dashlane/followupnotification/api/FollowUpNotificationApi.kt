package com.dashlane.followupnotification.api

import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject

interface FollowUpNotificationApi {
    fun startFollowUpNotification(summaryObject: SummaryObject, copyField: CopyField?)
    fun refreshExistingFollowUpNotification(followUpNotificationId: String)
    fun dismissFollowUpNotifications(followUpNotificationId: String, autoDismiss: Boolean)
    fun copyToClipboard(followUpNotificationId: String, copyFieldIndex: Int)
}