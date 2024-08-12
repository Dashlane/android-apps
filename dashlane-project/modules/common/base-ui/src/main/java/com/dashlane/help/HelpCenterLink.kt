package com.dashlane.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.browser.customtabs.CustomTabsIntent
import com.dashlane.ui.R
import com.dashlane.util.URIBuilder
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab
import java.net.URI
import java.util.Locale

sealed class HelpCenterLink {
    data object Base : HelpCenterLink()
    data class Article(
        val id: String,
        val anchor: String? = null
    ) : HelpCenterLink()

    val uri: URI
        get() = URIBuilder(
            scheme = "https",
            authority = "support.dashlane.com"
        ).apply {
            appendPath("hc")
            appendPath(helpCenterLanguage)
            if (this@HelpCenterLink is Article) {
                appendPath("articles")
                appendPath(id)
                anchor?.let { fragment = it }
            }
            appendQueryParameter("utm_source", "dashlane_app")
            appendQueryParameter("utm_medium", "android")
        }.build()

    val androidUri: Uri
        get() = Uri.parse(uri.toString())

    companion object {
        val ARTICLE_CANNOT_LOGIN = Article("202698981")
        val ARTICLE_FORGOT_PASSWORD = Article("115003111325")
        val ARTICLE_THIRD_PARTY_VPN = Article("360001158385")
        val ARTICLE_THIRD_PARTY_VPN_HOW_TO = Article("360000045439")
        val ARTICLE_THIRD_PARTY_VPN_FAQ = Article("360000037900")
        val ARTICLE_IMPORT_FROM_CHROME = Article("360003320260", "app")

        @JvmField
        val ARTICLE_AUTOFILL_WARNING = Article("202734501", "title5.3")
        val ARTICLE_IDENTITY_RESTORATION = Article("360000040279")
        val ARTICLE_IDENTITY_PROTECTION = Article("360000040299")
        val ARTICLE_CSV_IMPORT = Article("360005128380", "title2")
        val ARTICLE_CSV_EXPORT_WEB = Article("202625092")
        val ARTICLE_SSO_LOGIN = Article("360015304999", "title2")
        val ARTICLE_AUTHENTICATOR = Article("115003383365")
        val ARTICLE_AUTHENTICATOR_APP = Article("4583048536082")
        val ARTICLE_USE_RECOVERY_CODE = Article("202699101")
        val ARTICLE_MASTER_PASSWORDLESS_ACCOUNT_INFO = Article("10975547141266")
        val ARTICLE_ACCOUNT_RECOVERY_OPTIONS = Article("11282971791634")
        val ARTICLE_ABOUT_FREE_PLAN_CHANGES = Article("14324287429522")
        val ARTICLE_AUTHENTICATOR_SUNSET = Article("4583048536082")

        @get:VisibleForTesting
        val helpCenterLanguage: String
            get() = when (val lang = Locale.getDefault().language) {
                "fr", "de", "es" -> lang
                else -> "en-us"
            }
    }
}

fun HelpCenterLink.newIntent(
    context: Context
): Intent {
    return CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setExitAnimations(context, -1, R.anim.fadeout_fragment)
        .applyAppTheme()
        .build()
        .intent
        .setData(androidUri)
        .fallbackCustomTab(context.packageManager)
}
