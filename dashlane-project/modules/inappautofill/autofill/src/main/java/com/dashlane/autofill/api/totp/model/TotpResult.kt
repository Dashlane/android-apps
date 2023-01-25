package com.dashlane.autofill.api.totp.model



data class TotpResult(
    val code: String,
    val timeRemainingMilliseconds: Long
)