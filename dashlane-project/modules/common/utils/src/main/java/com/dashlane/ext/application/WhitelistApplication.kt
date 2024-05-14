package com.dashlane.ext.application

import android.content.Context
import com.dashlane.core.helpers.AppSignature
import com.dashlane.url.toUrlDomain
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

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
        val sha256: List<String>?
    )
}

data class WhitelistApplicationImpl @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context
) : WhitelistApplication {

    override val whitelistApplication: List<KnownApplication.App> by lazy {
        applicationContext.assets.open("whitelist-apps.json")
            .reader()
            .use {
                Gson().fromJson<List<WhitelistApplication.WhitelistApps>>(
                    it,
                    object : TypeToken<List<WhitelistApplication.WhitelistApps>>() {}.type
                )
            }.map {
                KnownApplication.App(
                    packageName = it.packageName,
                    signatures = AppSignature(it.packageName, it.sha256),
                    mainUrlDomain = it.website.toUrlDomain(),
                    allowedDomains = null,
                    keywords = null
                )
            }
    }
}