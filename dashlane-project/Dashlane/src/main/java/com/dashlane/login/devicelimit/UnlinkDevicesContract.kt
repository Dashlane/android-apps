package com.dashlane.login.devicelimit

import android.os.Bundle
import com.dashlane.login.Device

interface UnlinkDevicesContract {

    interface ViewProxy {
        

        fun showDevices(devices: List<Device>)
    }

    interface Presenter {
        var viewProxy: ViewProxy?

        

        fun onCreate(savedInstanceState: Bundle?)

        

        fun onSaveInstanceState(outState: Bundle)

        

        fun onCancelUnlink()

        

        fun onUnlink(selectedDevices: List<Device>)

        

        fun onBackPressed()
    }
}