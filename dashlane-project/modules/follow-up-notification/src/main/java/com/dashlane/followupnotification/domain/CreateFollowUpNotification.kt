package com.dashlane.followupnotification.domain

import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject



interface CreateFollowUpNotification {
    fun createFollowUpNotification(summaryObject: SummaryObject, copyField: CopyField?): FollowUpNotification?
    fun refreshExistingNotification(currentNotification: FollowUpNotification): FollowUpNotification?
}
