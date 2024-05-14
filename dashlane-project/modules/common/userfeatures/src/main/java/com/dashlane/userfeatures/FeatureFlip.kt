package com.dashlane.userfeatures

enum class FeatureFlip(val value: String) {
    @Deprecated("Prefer using the Extension function [UserFeaturesChecker.canUseSecureNotes] to verify if the user has all the permissions to use Secure Notes.")
    DISABLE_SECURE_NOTES("disableSecureNotes"),

    ATTACHMENT_ALL_ITEMS("attachmentAllItems_android"),

    SENTRY_NON_FATAL("sentry_non_fatal-1"),

    SHOW_ALLOW_SEND_LOGS("show_allow_send_logs-1"),

    SPECIAL_PRIDE_MODE("platform_android_prideColors_v2"),

    DASHLANE_LABS("techWeek_android_displayLabs"),

    AUTOMATICALLY_COPY_2FA("autofill_android_automaticallyCopy2fa"),

    VAULT_ACTIVITY_LOGS("android_vault_audit_logs"),

    SHARING_COLLECTION("sharing_android_collectionSharing"),

    SHARING_COLLECTION_ROLES("sharingVault_android_collectionEditorManager")
}