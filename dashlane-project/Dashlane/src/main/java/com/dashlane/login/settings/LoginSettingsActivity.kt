package com.dashlane.login.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.dashlane.R
import com.dashlane.login.LoginSsoLoggerConfigProvider
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.sso.LoginSsoLogger
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.usage.getUsageLogCode2SenderFromOrigin
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginSettingsActivity : DashlaneActivity(), LoginSsoLoggerConfigProvider {

    override val ssoLoggerConfig
        get() = LoginSsoLogger.Config(
            trackingId,
            InstallLogCode69.Type.LOGIN,
            intent.getStringExtra(LockSetting.EXTRA_DOMAIN),
            getUsageLogCode2SenderFromOrigin(intent)
        )

    @Inject
    lateinit var loginSettingsPresenter: LoginSettingsPresenter

    @Inject
    @TrackingId
    lateinit var trackingId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        val rootView = LayoutInflater.from(this).inflate(R.layout.activity_login_settings, null, false)
        setContentView(rootView)

        loginSettingsPresenter.setView(LoginSettingsViewProxy(rootView, loginSettingsPresenter.logger))
    }

    override fun onBackPressed() {
        
    }
}