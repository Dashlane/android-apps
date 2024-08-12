package com.dashlane.login.devicelimit

import android.annotation.SuppressLint
import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.getDevicesLimitValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeviceLimitActivity : DashlaneActivity() {

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    @Inject
    lateinit var presenter: DeviceLimitContract.Presenter

    lateinit var viewProxy: DeviceLimitViewProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning)

        viewProxy = DeviceLimitViewProxy(
            activity = this,
            maxDevices = userFeaturesChecker.getDevicesLimitValue()
        )
        viewProxy.presenter = presenter
        if (savedInstanceState == null) {
            presenter.onShow()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        
    }
}
