package com.dashlane.inappautofill

import android.annotation.SuppressLint
import javax.inject.Inject

class InAppLoginManager @Inject constructor(val autoFillApiManager: AutoFillApiManager) {
    fun isEnableForApp(): Boolean {
        return autoFillApiManager.getAutofillStatus() == AutoFillApiManager.AutofillStatus.ENABLED
    }

    fun isDisabledForApp(): Boolean {
        return autoFillApiManager.getAutofillStatus() == AutoFillApiManager.AutofillStatus.DISABLED
    }

    fun startActivityToDisableProvider() = autoFillApiManager.createAutofillSettingsPendingIntent(false).send()

    @SuppressLint("SwitchIntDef")
    fun isEnable(): Boolean {
        return autoFillApiManager.getAutofillStatus() == AutoFillApiManager.AutofillStatus.ENABLED
    }

    fun hasAutofillApiDisabled(): Boolean = !isEnable()
}