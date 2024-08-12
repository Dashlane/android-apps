package com.dashlane.login.monobucket

import com.dashlane.login.Device
import com.dashlane.login.getMostRecentDevice
import com.dashlane.login.toDevice
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.featureflipping.UserFeaturesChecker

class MonobucketHelper(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val listDevicesData: ListDevicesService.Data
) {

    fun getMonobucketOwner(): Device? {
        val isPremiumUser = userFeaturesChecker.has(Capability.SYNC)
        if (isPremiumUser) return null

        return getBucketOwner()
    }

    private fun getBucketOwner() = getBucketOwnerDevice() ?: getBucketOwnerPairingGroup()

    private fun getBucketOwnerDevice() = listDevicesData.devices.firstOrNull { it.isBucketOwner == true }?.toDevice()

    private fun getBucketOwnerPairingGroup(): Device? {
        val bucketOwnerPairingGroup =
            listDevicesData.pairingGroups.firstOrNull { it.isBucketOwner == true } ?: return null
        val devices = listDevicesData.devices
        return bucketOwnerPairingGroup.getMostRecentDevice(devices)
    }
}