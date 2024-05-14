package com.dashlane.autofill.passwordendpoint

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.autofill.internal.AutofillApiEntryPoint

open class OsSettingsRedirectionActivity : AppCompatActivity() {
    private val autofillComponent: AutofillApiEntryPoint
        get() = AutofillApiEntryPoint(this)

    protected val navigationService by lazy { autofillComponent.navigationService }
}

class SettingsRedirectionActivity : OsSettingsRedirectionActivity() {
    override fun onResume() {
        super.onResume()
        navigationService.navigateToAutofillSettings(
            activity = this,
            startAsNewTask = true
        )
        finish()
    }
}

class VaultRedirectionActivity : OsSettingsRedirectionActivity() {
    override fun onResume() {
        super.onResume()
        navigationService.navigateToPasswordSection(
            activity = this,
            startAsNewTask = true
        )
        finish()
    }
}