package com.dashlane.inapplogin

import android.annotation.SuppressLint
import android.content.Context
import javax.inject.Inject

class InAppLoginManager @Inject constructor(
    val autoFillApiManager: AutoFillApiManager?
) {
    fun isEnableForApp(): Boolean {
        return autoFillApiManager?.getAutofillStatus() == AutoFillApiManager.AutofillStatus.ENABLED
    }

    fun isDisabledForApp(): Boolean {
        return autoFillApiManager?.getAutofillStatus() == AutoFillApiManager.AutofillStatus.DISABLED
    }

    fun startActivityToDisableProvider(context: Context): Boolean {
        return autoFillApiManager?.startActivityToDisableProvider(context) == true
    }

    @SuppressLint("SwitchIntDef")
    fun isEnable(): Boolean {
        return autoFillApiManager?.getAutofillStatus() == AutoFillApiManager.AutofillStatus.ENABLED
    }

    fun hasAutofillApiDisabled(): Boolean = !isEnable()
}