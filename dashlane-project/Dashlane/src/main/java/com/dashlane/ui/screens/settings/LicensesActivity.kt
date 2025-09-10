package com.dashlane.ui.screens.settings

import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.screens.settings.licences.License
import com.dashlane.ui.screens.settings.licences.LicensesScreen
import com.dashlane.util.UiConstants
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab
import com.dashlane.util.readString
import com.dashlane.util.safelyStartBrowserActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

class LicensesActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val licensesInfo = loadLicenses().values.toList()
        val licenseAssets = CachedLicenseAssets(assets)
        val licenses = licensesInfo.map { license ->
            License(
                title = license.name,
                description = license.getCopyright(licenseAssets),
                websiteUrl = license.url,
                licenseUrl = license.licenseUrl,
            )
        }

        setContent {
            DashlaneTheme {
                LicensesScreen(
                    onNavigateUp = { onNavigateUp() },
                    licences = licenses,
                    openUrl = ::openUrl,
                )
            }
        }
    }

    private fun loadLicenses() = assets.open("licenses/index.json").reader().use { reader ->
        Gson().fromJson<Map<String, License>>(reader, object : TypeToken<Map<String, License>>() {}.type)
    }

    private fun openUrl(url: String) {
        val browserIntent = CustomTabsIntent.Builder().apply {
            applyAppTheme()
            setShowTitle(true)
            setExitAnimations(
                this@LicensesActivity,
                UiConstants.ANIMATION.ANIMATION_IN_SLIDE,
                UiConstants.ANIMATION.ANIMATION_OUT_SLIDE,
            )
        }.build().intent.apply {
            data = Uri.parse(url)
            fallbackCustomTab(packageManager)
        }
        safelyStartBrowserActivity(browserIntent)
    }

    private class CachedLicenseAssets(private val assets: AssetManager) {
        private val assetCache = mutableMapOf<String, String>()

        operator fun get(fileName: String) =
            assetCache.getOrPut(fileName) { assets.readString("licenses/$fileName") }
    }

    private class License(
        @SerializedName("name")
        val name: String,
        @SerializedName("copyright")
        private val copyright: String?,
        @SerializedName("copyrightAsset")
        private val copyRightAsset: String?,
        @SerializedName("copyrightTemplate")
        private val copyrightTemplate: LicenseTemplate?,
        @SerializedName("url")
        val url: String,
        @SerializedName("license")
        val licenseUrl: String,
    ) {
        fun getCopyright(licenseAssets: CachedLicenseAssets) =
            copyright
                ?: copyrightTemplate?.let {
                    val asset = when (val licenseType = it.type) {
                        "apache" -> "template_apache.txt"
                        "mit" -> "template_mit.txt"
                        "bsd3" -> "template_bsd_3_clause.txt"
                        else -> error("Unknown license $licenseType")
                    }
                    licenseAssets[asset]
                        .replaceFirst("[year]", it.year)
                        .replaceFirst("[author]", it.author)
                }
                ?: copyRightAsset?.let { licenseAssets[it] } ?: ""
    }

    private class LicenseTemplate(
        @SerializedName("year")
        val year: String,
        @SerializedName("author")
        val author: String,
        @SerializedName("type")
        val type: String,
    )
}