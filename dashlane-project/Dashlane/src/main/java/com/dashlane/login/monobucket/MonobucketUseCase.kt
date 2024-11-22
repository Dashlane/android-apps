package com.dashlane.login.monobucket

import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.login.Device
import com.dashlane.login.getMostRecentDevice
import com.dashlane.login.toDevice
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability

fun getMonobucketOwner(userFeaturesChecker: UserFeaturesChecker, listDevicesData: ListDevicesService.Data): Device? {
    val isPremiumUser = userFeaturesChecker.has(Capability.SYNC)
    if (isPremiumUser) return null

    return listDevicesData.devices.firstOrNull { it.isBucketOwner == true }?.toDevice()
        ?: run {
            val bucketOwnerPairingGroup = listDevicesData.pairingGroups.firstOrNull { it.isBucketOwner == true } ?: return null
            bucketOwnerPairingGroup.getMostRecentDevice(listDevicesData.devices)
        }
}
