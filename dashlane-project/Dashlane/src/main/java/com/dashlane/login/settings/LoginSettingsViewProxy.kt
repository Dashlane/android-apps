package com.dashlane.login.settings

import android.view.View
import android.widget.Button
import com.dashlane.R
import com.dashlane.settings.biometric.BiometricSettingsHelper
import com.dashlane.settings.biometric.BiometricSettingsHelperImpl
import com.skocken.presentation.viewproxy.BaseViewProxy

class LoginSettingsViewProxy(
    rootView: View,
    private val biometricSettingsHelper: BiometricSettingsHelper = BiometricSettingsHelperImpl(rootView)
) : BaseViewProxy<LoginSettingsContract.Presenter>(rootView),
LoginSettingsContract.ViewProxy,
    BiometricSettingsHelper by biometricSettingsHelper {

    init {
        findViewByIdEfficient<Button>(R.id.next_btn)!!.setOnClickListener {
            presenter.onNext()
        }
    }
}