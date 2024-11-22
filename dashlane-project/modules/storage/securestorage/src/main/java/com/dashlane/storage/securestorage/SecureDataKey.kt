package com.dashlane.storage.securestorage

import androidx.annotation.StringDef

class SecureDataKey {
    companion object {
        const val LOCAL_KEY = "lk"

        const val SECRET_KEY = "uki"

        const val SETTINGS = "settings"

        
        const val USER_FEATURE_FLIPS = "user_feature_flips"
        const val RSA_PRIVATE_KEY = "rsa_private_key"
        const val PIN_CODE = "pin_code"
        const val LEGACY_PREMIUM_SERVER_RESPONSE = "premium_server_response"
        const val ACCOUNT_STATUS_CACHE = "account_status_cache"
        const val MASTER_PASSWORD = "mp"
        const val REMOTE_KEY = "remote_key"
        const val SERVER_KEY = "server_key"

        const val DEVICE_ANALYTICS_ID = "deviceAnalyticsId"

        const val USER_ANALYTICS_ID = "userAnalyticsId"

        const val USER_CONTACT_EMAIL = "userContactEmail"

        const val USER_ACTIVITY = "userActivity"

        const val CURRENT_SPACE_ID_FILTER = "currentSpaceIdFilter"
    }

    @Suppress("DEPRECATION")
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        LOCAL_KEY,
        SECRET_KEY,
        RSA_PRIVATE_KEY,
        PIN_CODE,
        USER_FEATURE_FLIPS,
        LEGACY_PREMIUM_SERVER_RESPONSE,
        ACCOUNT_STATUS_CACHE,
        SETTINGS,
        MASTER_PASSWORD,
        REMOTE_KEY,
        DEVICE_ANALYTICS_ID,
        USER_ANALYTICS_ID,
        USER_CONTACT_EMAIL,
        SERVER_KEY,
        USER_ACTIVITY,
        CURRENT_SPACE_ID_FILTER
    )
    annotation class Key
}
