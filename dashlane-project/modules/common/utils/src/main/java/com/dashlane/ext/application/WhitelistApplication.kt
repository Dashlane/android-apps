package com.dashlane.ext.application

import com.google.gson.annotations.SerializedName

interface WhitelistApplication {
    val whitelistApplication: List<KnownApplication.App>

    fun getAppForPackage(packageName: String): KnownApplication.App? =
        whitelistApplication.singleOrNull { it.packageName == packageName }

    data class WhitelistApps(
        @SerializedName("packageName")
        val packageName: String,
        @SerializedName("website")
        val website: String,
        @SerializedName("sha256")
        val sha256: List<String>?,
    )
}