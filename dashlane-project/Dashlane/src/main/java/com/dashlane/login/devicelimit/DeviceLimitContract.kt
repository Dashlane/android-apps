package com.dashlane.login.devicelimit

interface DeviceLimitContract {

    interface ViewProxy

    interface Presenter {
        fun onUpgradePremium()

        fun onUnlinkPreviousDevices()

        fun onLogOut()

        fun onStart()

        fun onShow()
    }
}