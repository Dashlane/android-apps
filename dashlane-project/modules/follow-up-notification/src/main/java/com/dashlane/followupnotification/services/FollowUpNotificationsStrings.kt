package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.util.clipboard.vault.CopyField



interface FollowUpNotificationsStrings {
    fun getFollowUpNotificationsTypesLabels(followUpNotificationsTypes: FollowUpNotificationsTypes): String
    fun getFieldLabel(copyField: CopyField): String?
    fun getFollowUpNotificationsTitle(): String
    fun getFollowUpNotificationsDescription(): String
}