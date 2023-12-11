package com.dashlane.login.settings

import com.dashlane.settings.biometric.BiometricSettingsHelper
import com.skocken.presentation.definition.Base

interface LoginSettingsContract {
    interface Presenter : Base.IPresenter {
        fun onNext()
    }

    interface ViewProxy : Base.IView, BiometricSettingsHelper
}