package com.dashlane.autofill

import android.content.Context
import android.provider.Settings
import com.dashlane.util.isNotSemanticallyNull

class AutofillCompatModeAllowedPackages(val compatSetting: String? = null) {

    private val compatEntriesByPackageName: Map<String, CompatAppEntry>? = compatSetting.toListCompatApps()

    private data class CompatAppEntry(
        val packageName: String,
        val urlBarIds: List<String>
    )

    fun isDefined(): Boolean {
        return compatEntriesByPackageName != null
    }

    fun isAppDeclared(packageName: String): Boolean {
        return compatEntriesByPackageName?.containsKey(packageName) ?: return false
    }

    fun matchAppUrlBarId(packageName: String, urlBarId: String): Boolean {
        return compatEntriesByPackageName?.get(packageName)?.urlBarIds?.contains(urlBarId) ?: return false
    }

    fun getAppUrlBarIds(packageName: String): List<String>? {
        return compatEntriesByPackageName?.get(packageName)?.urlBarIds
    }

    fun getAppPackageNames(): List<String>? {
        return compatEntriesByPackageName?.keys?.toList()
    }

    private fun String?.toListCompatApps(): Map<String, CompatAppEntry>? {
        val compatEntries = this?.split(COMPAT_ENTRY_SEPARATOR)
            .takeUnless { it.isNullOrEmpty() } ?: return null

        return compatEntries.mapNotNull {
            val compatApp = it.toCompatApp() ?: return@mapNotNull null
            compatApp.packageName to compatApp
        }.toMap().takeIf { it.isNotEmpty() }
    }

    private fun String.toCompatApp(): CompatAppEntry? {
        val indexOfStartBracket =
            this.indexOf(URL_BAR_ID_START_ARRAY).takeIf { it != -1 } ?: return null
        val indexOfEndBracket =
            this.indexOf(URL_BAR_ID_END_ARRAY).takeIf { it != -1 } ?: return null
        val packageName = this.substring(0, indexOfStartBracket).takeIf { it.isNotSemanticallyNull() } ?: return null
        val urlBarIds = this.substring(indexOfStartBracket + 1, indexOfEndBracket)
            .split(URL_BAR_ID_SEPARATOR).takeIf { it.hasNoSemanticallyNulls() }
            ?: return null

        return CompatAppEntry(packageName, urlBarIds)
    }

    private fun Iterable<String?>.hasNoSemanticallyNulls(): Boolean {
        return this.all {
            it.isNotSemanticallyNull()
        }
    }

    companion object {
        const val COMPAT_ENTRY_SEPARATOR = ":"
        const val URL_BAR_ID_SEPARATOR = ","
        const val URL_BAR_ID_START_ARRAY = "["
        const val URL_BAR_ID_END_ARRAY = "]"
    }
}

fun Context.getAutofillCompatModeAllowedPackagesFromSettings(): AutofillCompatModeAllowedPackages {
    val autofillCompatModeAllowedPackagesSetting =
        Settings.Global.getString(this.contentResolver, "autofill_compat_mode_allowed_packages")

    return AutofillCompatModeAllowedPackages(autofillCompatModeAllowedPackagesSetting)
}
