package com.dashlane.device.component

import android.content.Context
import com.dashlane.device.DeviceInfoRepository



interface DeviceInfoRepositoryComponent {
    val deviceInfoRepository: DeviceInfoRepository

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as DeviceInfoRepositoryApplication).component
    }
}