package com.dashlane.autofill.totp

data class TotpResult(
    val code: String,
    val timeRemainingMilliseconds: Long
)