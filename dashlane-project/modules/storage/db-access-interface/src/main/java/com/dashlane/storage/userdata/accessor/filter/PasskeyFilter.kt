package com.dashlane.storage.userdata.accessor.filter

sealed class PasskeyFilter {
    abstract val allowsLocalPasskeys: Boolean
    abstract val allowsCloudPasskeys: Boolean

    data object AllPasskeysFilter : PasskeyFilter() {
        override val allowsLocalPasskeys: Boolean = true
        override val allowsCloudPasskeys: Boolean = true
    }

    data object LocalPasskeysFilter : PasskeyFilter() {
        override val allowsLocalPasskeys: Boolean = true
        override val allowsCloudPasskeys: Boolean = false
    }
}