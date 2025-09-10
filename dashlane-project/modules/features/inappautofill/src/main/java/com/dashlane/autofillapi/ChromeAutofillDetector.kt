package com.dashlane.autofillapi

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import com.dashlane.autofillapi.ChromeAutofillStatus.ChromePackage
import com.dashlane.autofillapi.ChromeAutofillStatus.CompatibilityAutofill
import com.dashlane.autofillapi.ChromeAutofillStatus.DefaultAutofill
import com.dashlane.autofillapi.ChromeAutofillStatus.ThirdPartyAutofillActivated
import com.dashlane.util.tryOrNull
import javax.inject.Inject

class ChromeAutofillDetector @Inject constructor() {
    companion object {
        private const val CHROME_CONTENT_PROVIDER_NAME = ".AutofillThirdPartyModeContentProvider"
        private const val CHROME_THIRD_PARTY_MODE_COLUMN = "autofill_third_party_state"
        private const val CHROME_THIRD_PARTY_MODE_ACTIONS_URI_PATH = "autofill_third_party_mode"
    }

    fun getChromeAutofillStatus(contentResolver: ContentResolver, packageManager: PackageManager): ChromeAutofillStatus {
        val installedPackages = packageManager.listChromeInstalled()
        if (installedPackages.isEmpty()) return CompatibilityAutofill()
        installedPackages.forEach { chromePackage ->
            if (isChromeVersionCompatible(chromePackage, packageManager)) {
                val uri = Uri
                    .Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(chromePackage.packageName + CHROME_CONTENT_PROVIDER_NAME)
                    .path(CHROME_THIRD_PARTY_MODE_ACTIONS_URI_PATH)
                    .build()
                val status = queryChromeAutofillStatus(contentResolver, uri)
                when (status) {
                    0 -> return DefaultAutofill(chromePackage.packageName)
                    1 -> return ThirdPartyAutofillActivated(chromePackage.packageName)
                }
            }
        }
        return CompatibilityAutofill()
    }

    fun isThirdPartyAutofillDeactivated(chromeAutofillStatus: ChromeAutofillStatus) =
        chromeAutofillStatus !is CompatibilityAutofill && chromeAutofillStatus !is ThirdPartyAutofillActivated

    fun isThirdPartyAutofillDeactivated(contentResolver: ContentResolver, packageManager: PackageManager): Boolean {
        val chromeAutofillStatus = getChromeAutofillStatus(contentResolver, packageManager)
        return isThirdPartyAutofillDeactivated(chromeAutofillStatus)
    }

    private fun isChromeVersionCompatible(chromePackage: ChromePackage, packageManager: PackageManager): Boolean {
        val version = tryOrNull {
            packageManager
                .getPackageInfo(chromePackage.packageName, PackageManager.GET_ACTIVITIES)
                ?.versionName
                ?.substringBefore(".")
                ?.toInt()
        } ?: return false
        return version >= 135
    }

    private fun PackageManager.listChromeInstalled(): List<ChromePackage> {
        val installedPackages = mutableListOf<ChromePackage>()
        ChromePackage.entries.forEach { chromePackage ->
            try {
                getPackageInfo(chromePackage.packageName, PackageManager.GET_ACTIVITIES)
                installedPackages.add(chromePackage)
            } catch (e: NameNotFoundException) {
                
            }
        }
        return installedPackages
    }

    private fun queryChromeAutofillStatus(contentResolver: ContentResolver, uri: Uri): Int? {
        val cursor = contentResolver.query(uri, arrayOf(CHROME_THIRD_PARTY_MODE_COLUMN), null, null, null)
            
            ?: return null
        cursor.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(CHROME_THIRD_PARTY_MODE_COLUMN)
                if (index == -1) return null
                return it.getInt(index)
            }
            return null
        }
    }
}