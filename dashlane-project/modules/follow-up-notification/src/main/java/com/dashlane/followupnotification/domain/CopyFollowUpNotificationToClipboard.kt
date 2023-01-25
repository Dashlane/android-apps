package com.dashlane.followupnotification.domain



interface CopyFollowUpNotificationToClipboard {
    fun copy(followUpNotification: FollowUpNotification, copyFieldIndex: Int): Boolean
}
