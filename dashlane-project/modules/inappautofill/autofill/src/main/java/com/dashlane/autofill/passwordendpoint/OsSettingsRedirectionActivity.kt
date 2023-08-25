package com.dashlane.autofill.passwordendpoint

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.autofill.api.internal.AutofillApiComponent

open class OsSettingsRedirectionActivity : AppCompatActivity() {
    private val autofillComponent: AutofillApiComponent
        get() = AutofillApiComponent(this)

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