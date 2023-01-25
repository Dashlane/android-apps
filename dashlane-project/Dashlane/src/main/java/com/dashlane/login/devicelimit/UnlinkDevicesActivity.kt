package com.dashlane.login.devicelimit

import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.getDevicesLimitValue
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class UnlinkDevicesActivity : DashlaneActivity() {

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    @Inject
    lateinit var presenter: UnlinkDevicesContract.Presenter

    lateinit var viewProxy: UnlinkDevicesViewProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_limit_unlink_devices)

        val maxDevices = userFeaturesChecker.getDevicesLimitValue()
        viewProxy = UnlinkDevicesViewProxy(activity = this, maxDevices = maxDevices)
        viewProxy.presenter = presenter
        presenter.viewProxy = viewProxy
        presenter.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_DEVICES = "extra_devices"
    }
}
