package com.dashlane.login

import android.os.Parcelable
import com.dashlane.R
import com.dashlane.vault.model.DeviceType
import com.dashlane.login.devicelimit.DeviceViewHolder
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.time.toInstant
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val id: String,
    val name: String,
    val iconResId: Int,
    val lastActivityDate: Long,
    val pairingGroupId: String? = null,
    var selected: Boolean = false
) : Parcelable, DashlaneRecyclerAdapter.ViewTypeProvider {
    override fun getViewType() = DashlaneRecyclerAdapter.ViewType(R.layout.item_device, DeviceViewHolder::class.java)
}

fun ListDevicesService.Data.Device.toDevice(groupId: String? = null) = Device(
    id = id,
    name = name.orEmpty(),
    iconResId = DeviceType.forValue(platform).iconResId,
    lastActivityDate = lastActivityDate.toInstant().toEpochMilli(),
    pairingGroupId = groupId
)

fun ListDevicesService.Data.PairingGroup.getMostRecentDevice(allDevices: List<ListDevicesService.Data.Device>): Device? {
    val deviceIds = deviceIds.toSet()
    val devices = allDevices.filter { it.id in deviceIds }
    if (devices.isEmpty()) return null
    val desktopDevices = devices.filter { DeviceType.forValue(it.platform).isDesktop }
    return if (desktopDevices.isEmpty()) {
        devices.maxByOrNull { it.updateDate }?.toDevice(id)
    } else {
        desktopDevices.maxByOrNull { it.updateDate }?.toDevice(id)
    }
}