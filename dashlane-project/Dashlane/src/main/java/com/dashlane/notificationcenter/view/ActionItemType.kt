package com.dashlane.notificationcenter.view

enum class ActionItemType(val trackingKey: String) {
    AUTO_FILL("autologin"),
    PIN_CODE("pincode"),
    BIOMETRIC("biometric"),
    ZERO_PASSWORD("zero_password"),
    BREACH_ALERT("breach_alert"),
    SHARING("sharing_invitation"),
    ACCOUNT_RECOVERY("account_recovery"),
    FREE_TRIAL_STARTED("free_trial_started"),
    TRIAL_UPGRADE_RECOMMENDATION("trial_upgrade_recommendation"),
    AUTHENTICATOR_ANNOUNCEMENT("authenticator_announcement"),
    INTRODUCTORY_OFFERS("introductory_offers")
}