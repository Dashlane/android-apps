package com.dashlane.login.devicelimit

import android.os.Bundle
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.getDevicesLimitValue
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
        clearLoadAccountLogger()
        presenter.onStart()
    }

    override fun onBackPressed() {
        
    }

    private fun clearLoadAccountLogger() {
        SingletonProvider.getComponent().timeToLoadLocalLogger.clear()
        SingletonProvider.getComponent().timeToLoadRemoteLogger.clear()
    }
}
