package com.dashlane.inapplogin

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.autofill.AutofillManager
import com.dashlane.util.tryOrNull

class InAppLoginByAutoFillApiManager private constructor(private val context: Context) {

    private val autoFillManager: AutofillManager?
        get() = context.getSystemService(AutofillManager::class.java)

    fun getAutofillStatus() = when (tryOrNull { autoFillManager?.hasEnabledAutofillServices() }) {
        true -> AutofillStatus.ENABLED
        false -> AutofillStatus.DISABLED
        else -> AutofillStatus.UNKNOWN
    }

    fun startActivityToChooseProvider(context: Context): Boolean {
        return startProviderChooser(context, "com.dashlane")
    }

    fun startActivityToDisableProvider(context: Context): Boolean {
        return startProviderChooser(context, "none")
    }

    private fun startProviderChooser(context: Context, packageName: String): Boolean {
        if (autoFillManager?.isAutofillSupported == false) {
            return false
        }
        return try {
            val intent = autofillSettingIntent(packageName)
            context.startActivity(intent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    fun startActivityToChooseProviderForResult(activity: Activity): Boolean {
        return startProviderChooserForResult(activity, "com.dashlane")
    }

    fun startActivityToDisableProviderForResult(activity: Activity): Boolean {
        return startProviderChooserForResult(activity, "none")
    }

    private fun startProviderChooserForResult(activity: Activity, packageName: String): Boolean {
        if (autoFillManager?.isAutofillSupported == false) {
            return false
        }
        return try {
            val intent = autofillSettingIntent(packageName)
            activity.startActivityForResult(intent, SET_AUTOFILL_PROVIDER_REQUEST_CODE)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun autofillSettingIntent(packageName: String) = Intent(
        Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE,
        Uri.parse("package:" + packageName)
    )

    enum class AutofillStatus {
        ENABLED,
        DISABLED,
        UNKNOWN
    }

    companion object {
        const val SET_AUTOFILL_PROVIDER_REQUEST_CODE = 407405122

        @JvmStatic
        fun createIfPossible(context: Context): InAppLoginByAutoFillApiManager? {
            val inAppLoginByAutoFillApiManager = InAppLoginByAutoFillApiManager(context)
            return if (inAppLoginByAutoFillApiManager.autoFillManager?.isAutofillSupported == true) {
                inAppLoginByAutoFillApiManager
            } else {
                null
            }
        }
    }
}