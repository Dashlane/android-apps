package com.dashlane.vault.history

enum class DataChangeHistoryField(val field: String) {
    EMAIL("Email"),
    LOGIN("Login"),
    NOTE("Note"),
    PASSWORD("Password"),
    TITLE("Title"),
    URL("Url"),
    USER_SELECTED_URL("UserSelectedUrl"),
    OTP_SECRET("OtpSecret"),
    OTP_URL("OtpUrl"),
}