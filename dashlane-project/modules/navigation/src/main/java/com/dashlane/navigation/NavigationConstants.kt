@file:JvmName("NavigationConstants")

package com.dashlane.navigation



object NavigationConstants {
    const val USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN =
        "userComeFromExternalPushTokenNotificationAlreadyLoggedIn"
    const val USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION = "userComeFromExternalPushTokenNotification"
    const val USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER = "userComeFromExternalPushTokenNotificationUser"
    const val STARTED_WITH_INTENT = "startedWithIntent"
    const val FORCED_LOCK_SESSION_RESTORED = "forceLockSessionRestored"
    const val LOGIN_CALLED_FROM_INAPP_LOGIN = "loginCallFromInAppLoginBubble"

    const val SESSION_RESTORED_FROM_BOOT = "sessionRestoredFromBoot"
    const val HOME_FORCE_CHANGE_CONTENT = "home_force_change_content"
    const val INSTALL_REFERRER_EXTRA = "referrer"
    const val INSTALL_REFERRER_NAME_EXTRA = "referrer_name"
    const val INSTALL_REFERRER_ORIGIN_PACKAGE_EXTRA = "referrerOriginPackage"
    const val STARTED_FROM_ONBOARDING = "statedFromOnboarding"
}
