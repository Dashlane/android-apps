package com.dashlane.darkweb

import com.google.gson.annotations.SerializedName



data class DarkWebEmailStatus(
    @SerializedName("email") val email: String,
    @SerializedName("status") val status: String
) {

    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_PENDING = "pending"
        const val STATUS_DISABLED = "disabled"
    }
}