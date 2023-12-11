package com.dashlane.login.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.dashlane.R
import com.dashlane.login.dagger.TrackingId
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginSettingsActivity : DashlaneActivity() {

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

        loginSettingsPresenter.setView(LoginSettingsViewProxy(rootView))
    }

    override fun onBackPressed() {
        
    }
}