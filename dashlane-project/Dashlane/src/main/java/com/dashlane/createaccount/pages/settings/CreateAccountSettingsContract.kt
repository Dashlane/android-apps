package com.dashlane.createaccount.pages.settings

import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.settings.biometric.BiometricSettingsHelper
import com.skocken.presentation.definition.Base



interface CreateAccountSettingsContract {

    interface ViewProxy : Base.IView, BiometricSettingsHelper

    interface Presenter : CreateAccountBaseContract.Presenter

    interface DataProvider : CreateAccountBaseContract.DataProvider {
        val logger: CreateAccountSettingsLogger
    }
}
