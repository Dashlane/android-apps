package com.dashlane.autofill.api.monitorlog

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.autofill.getAutofillCompatModeAllowedPackagesFromSettings
import com.dashlane.util.PackageUtilities
import com.dashlane.util.resolveActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class MonitorAutofillDeviceInfoServiceImpl @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val userPreferencesAccess: AutofillAnalyzerDef.IUserPreferencesAccess,
    private val autofillConfiguration: AutofillConfiguration
) : MonitorAutofillDeviceInfoService {

    private val monitoredApps = mapOf(
        MonitoredApp.Browser.CHROME to Browser("com.android.chrome"),
        MonitoredApp.Browser.SAMSUNG_BROWSER to Browser("com.sec.android.app.sbrowser"),
        MonitoredApp.Keyboard.GBOARD to Keyboard("com.google.android.inputmethod.latin"),
        MonitoredApp.Keyboard.HONEYBOARD to Keyboard("com.samsung.android.honeyboard")
    )

    override fun getDeviceManufacturer(): String = Build.MANUFACTURER

    override fun getDeviceModel(): String = Build.MODEL

    override fun isDefault(monitoredApp: MonitoredApp): Boolean {
        return monitoredApp.toApp()?.isDefault(applicationContext) ?: return false
    }

    private fun MonitoredApp.toApp(): App? = monitoredApps[this]

    override fun matchAutofillConfiguration(monitoredBrowser: MonitoredApp.Browser): Boolean? =
        monitoredBrowser.toBrowser()?.matchUrlBarIdsInSettings(applicationContext)

    private fun MonitoredApp.Browser.toBrowser(): Browser? = monitoredApps[this] as? Browser

    override fun getVersionName(monitoredApp: MonitoredApp): String? =
        monitoredApp.toApp()?.getVersionName(applicationContext)

    override fun isAutofillByApiEnabled(): Boolean = autofillConfiguration.hasAutofillByApiEnabled()

    override fun isAutofillByAccessibilityEnabled(): Boolean = autofillConfiguration.hasAccessibilityAutofillEnabled()

    override fun isAutofillByKeyboardEnabled(): Boolean = userPreferencesAccess.hasKeyboardAutofillEnabled()

    internal interface App {
        val packageName: String

        fun isDefault(context: Context): Boolean

        fun getVersionName(context: Context): String? =
            PackageUtilities.getPackageInfoFromPackage(context, packageName)?.versionName
    }

    internal class Browser(override val packageName: String) : App {

        override fun isDefault(context: Context): Boolean {
            val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("https://www.example.com"))
            val resolveInfo: ResolveInfo? =
                context.packageManager.resolveActivityCompat(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            val browserPackageName = resolveInfo?.activityInfo?.packageName ?: ""

            return packageName == browserPackageName
        }

        fun matchUrlBarIdsInSettings(context: Context): Boolean? =
            context.getAutofillCompatModeAllowedPackagesFromSettings().getAppUrlBarIds(packageName)?.any {
                BrowserDetectionHelper.isUrlBar(packageName, it)
            }
    }

    internal class Keyboard(override val packageName: String) : App {

        override fun isDefault(context: Context): Boolean {
            val imeManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val lst: List<InputMethodInfo> = imeManager.enabledInputMethodList
            val keyboardPackageName = lst.first { info ->
                info.id.equals(
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                )
            }.serviceInfo.applicationInfo.packageName

            return packageName == keyboardPackageName
        }
    }
}