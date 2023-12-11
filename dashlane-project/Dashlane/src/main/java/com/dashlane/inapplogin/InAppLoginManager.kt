package com.dashlane.inapplogin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.IntDef
import javax.inject.Inject

class InAppLoginManager @Inject constructor(
    val inAppLoginByAccessibilityManager: InAppLoginByAccessibilityManager,
    val inAppLoginByAutoFillApiManager: InAppLoginByAutoFillApiManager?
) {

    val intentOverlayPermissionIfRequire: Intent?
        get() {
            val permissionEnabled = inAppLoginByAccessibilityManager.isDrawOnTopPermissionEnabled()
            if (permissionEnabled) {
                return null
            }
            val uri = Uri.parse("package:com.dashlane")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }

    fun isEnableForApp(): Boolean {
        
        
        
        
        return if (inAppLoginByAutoFillApiManager != null) {
            inAppLoginByAutoFillApiManager.getAutofillStatus() == InAppLoginByAutoFillApiManager.AutofillStatus.ENABLED
        } else {
            inAppLoginByAccessibilityManager.isEnable()
        }
    }

    fun isDisabledForApp(): Boolean {
        return if (inAppLoginByAutoFillApiManager != null) {
            inAppLoginByAutoFillApiManager.getAutofillStatus() == InAppLoginByAutoFillApiManager.AutofillStatus.DISABLED
        } else {
            !inAppLoginByAccessibilityManager.isEnable()
        }
    }

    fun isEnableForChrome(): Boolean {
        
        return inAppLoginByAccessibilityManager.isEnable()
    }

    fun startActivityToChooseProvider(context: Context): Boolean {
        return inAppLoginByAutoFillApiManager?.startActivityToChooseProvider(context)
            ?: inAppLoginByAccessibilityManager.startActivityToChooseProvider(context)
    }

    fun startActivityToDisableProvider(context: Context): Boolean {
        return inAppLoginByAutoFillApiManager?.startActivityToDisableProvider(context)
            ?: inAppLoginByAccessibilityManager.startActivityToChooseProvider(context)
    }

    @SuppressLint("SwitchIntDef")
    fun isEnable(@Type inAppLoginType: Int): Boolean {
        return when (inAppLoginType) {
            TYPE_ACCESSIBILITY -> inAppLoginByAccessibilityManager.isEnable()
            TYPE_AUTO_FILL_API -> inAppLoginByAutoFillApiManager?.getAutofillStatus() == InAppLoginByAutoFillApiManager.AutofillStatus.ENABLED
            TYPE_NO_OP_ACCESSIBILITY -> inAppLoginByAccessibilityManager.isNoOpEnable()
            else -> false
        }
    }

    fun hasAutofillApiDisabled(): Boolean = !isEnable(TYPE_AUTO_FILL_API)

    companion object {
        const val TYPE_ACCESSIBILITY = 0
        const val TYPE_AUTO_FILL_API = 1
        const val TYPE_NO_OP_ACCESSIBILITY = 2
    }

    @IntDef(TYPE_ACCESSIBILITY, TYPE_AUTO_FILL_API, TYPE_NO_OP_ACCESSIBILITY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}