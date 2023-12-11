package com.dashlane.analytics.install

import android.content.Intent
import android.net.Uri
import com.dashlane.navigation.NavigationConstants
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.inject.Inject

class InstallTrackingManager @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager
) {

    fun installEvent(intent: Intent?) {
        globalPreferencesManager.putLong(ConstantsPrefs.INSTALLATION_TIMESTAMP, Instant.now().toEpochMilli())

        intent?.extras?.let { bundle ->
            try {
                val referrerExtra = bundle.getString(NavigationConstants.INSTALL_REFERRER_EXTRA) ?: return
                var referrerDecoded = URLDecoder.decode(referrerExtra, StandardCharsets.UTF_8.name())
                globalPreferencesManager.putString(ConstantsPrefs.FULL_REFERRER, referrerDecoded)
                if (!referrerDecoded.startsWith("?")) {
                    referrerDecoded = "?$referrerDecoded"
                }
                val dataUri = Uri.parse(referrerDecoded)
                parseReferrerExtra(dataUri)
                parseReferrerOrigin(dataUri)
            } catch (e: UnsupportedEncodingException) {
            }
        }
    }

    private fun parseReferrerExtra(uri: Uri?) {
        if (uri == null || !uri.isHierarchical) {
            return
        }

        val referrer = uri.getQueryParameter(NavigationConstants.INSTALL_REFERRER_NAME_EXTRA)
        if (referrer != null) {
            globalPreferencesManager.putString(ConstantsPrefs.REFERRED_BY, referrer)
        }
    }

    private fun parseReferrerOrigin(dataUri: Uri?) {
        if (dataUri == null || !dataUri.isHierarchical) {
            return
        }
        val referrerOriginPackage = dataUri.getQueryParameter(NavigationConstants.INSTALL_REFERRER_ORIGIN_PACKAGE_EXTRA)
        if (referrerOriginPackage != null) {
            globalPreferencesManager.putString(ConstantsPrefs.REFERRER_ORIGIN_PACKAGE, referrerOriginPackage)
        }
    }
}
