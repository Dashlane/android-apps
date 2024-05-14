package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.domain.FollowUpNotification
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject

interface VaultItemContentService {
    fun getContent(summaryObject: SummaryObject, copyField: CopyField): FollowUpNotification.FieldContent?
}
