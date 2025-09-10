package com.dashlane.notification.creator

interface AutoFillNotificationCreator {
    fun cancelAutofillNotificationWorkers()

    fun createForNewUser()

    fun createForExistingUser()

    companion object {
        const val NOTIFICATION_ID = 0x08
        const val CHROME_NOTIFICATION_ID = 0x09
        const val TAG_NEW_USER = "tag_new_user"
        const val TAG_EXISTING_USER = "tag_existing_user"
        const val TAG_USER_CHROME = "tag_user_chrome"
        const val DISMISSAL_THRESHOLD = 2
    }
}