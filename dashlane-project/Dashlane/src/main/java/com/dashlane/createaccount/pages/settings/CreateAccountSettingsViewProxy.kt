package com.dashlane.createaccount.pages.settings

import android.view.View
import com.dashlane.settings.biometric.BiometricSettingsHelper
import com.dashlane.settings.biometric.BiometricSettingsHelperImpl
import com.dashlane.settings.biometric.BiometricSettingsLogger
import com.skocken.presentation.viewproxy.BaseViewProxy

class CreateAccountSettingsViewProxy(
    rootView: View,
    logger: BiometricSettingsLogger,
    private val biometricSettingsHelper: BiometricSettingsHelper = BiometricSettingsHelperImpl(rootView, logger)
) : BaseViewProxy<CreateAccountSettingsContract.Presenter>
    (rootView),
    CreateAccountSettingsContract.ViewProxy,
    BiometricSettingsHelper by biometricSettingsHelper