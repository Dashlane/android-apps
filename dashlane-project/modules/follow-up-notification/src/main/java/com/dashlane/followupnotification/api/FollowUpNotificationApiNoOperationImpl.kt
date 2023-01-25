package com.dashlane.followupnotification.api

import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject



class FollowUpNotificationApiNoOperationImpl @Inject constructor() : FollowUpNotificationApi {
    override fun startFollowUpNotification(summaryObject: SummaryObject, copyField: CopyField?) = Unit
    override fun refreshExistingFollowUpNotification(followUpNotificationId: String) = Unit

    override fun dismissFollowUpNotifications(followUpNotificationId: String, autoDismiss: Boolean) = Unit
    override fun copyToClipboard(followUpNotificationId: String, copyFieldIndex: Int) = Unit
}