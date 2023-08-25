package com.dashlane.autofill.api.totp.services

interface TotpNotificationDisplayService {

    fun display(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    )

    fun updateWithNewCode(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    )

    fun updateInformingCodeCopied(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    )

    fun updateWithSafeguardCode(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    )

    fun dismissAll()
}
