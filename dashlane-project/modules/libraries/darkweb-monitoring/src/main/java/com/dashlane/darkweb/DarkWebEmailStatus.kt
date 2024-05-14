package com.dashlane.darkweb

data class DarkWebEmailStatus(
    val email: String,
    val status: String
) {

    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_PENDING = "pending"
        const val STATUS_DISABLED = "disabled"
    }
}