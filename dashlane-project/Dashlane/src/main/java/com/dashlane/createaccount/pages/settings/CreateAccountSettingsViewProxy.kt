package com.dashlane.createaccount.pages.settings

import android.view.View
import com.dashlane.settings.biometric.BiometricSettingsHelper
import com.dashlane.settings.biometric.BiometricSettingsHelperImpl
import com.skocken.presentation.viewproxy.BaseViewProxy

class CreateAccountSettingsViewProxy(
    rootView: View,
    private val biometricSettingsHelper: BiometricSettingsHelper = BiometricSettingsHelperImpl(rootView)
) : BaseViewProxy<CreateAccountSettingsContract.Presenter>
    (rootView),
    CreateAccountSettingsContract.ViewProxy,
    BiometricSettingsHelper by biometricSettingsHelper