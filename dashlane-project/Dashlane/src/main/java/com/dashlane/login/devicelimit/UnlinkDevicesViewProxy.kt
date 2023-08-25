package com.dashlane.login.devicelimit

import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.login.Device

class UnlinkDevicesViewProxy(val activity: FragmentActivity, private val maxDevices: Int) :
    UnlinkDevicesContract.ViewProxy {

    var presenter: UnlinkDevicesContract.Presenter? = null
    private val recyclerView: RecyclerView = activity.findViewById(R.id.unlink_devices_list)
    private val unlinkButton: Button = activity.findViewById(R.id.unlink_devices_unlink)
    private val deviceAdapter = DeviceAdapter(true).apply {
        itemCheckListener = object : DeviceAdapter.OnItemCheckListener {
            override fun onItemCheckChanged(item: Device) {
                updateState()
            }
        }
    }

    init {
        unlinkButton.apply {
            isEnabled = false
            setOnClickListener { presenter?.onUnlink(deviceAdapter.objects.filter { it.selected }) }
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = deviceAdapter
        }
        activity.findViewById<Button>(R.id.unlink_devices_cancel).setOnClickListener { presenter?.onCancelUnlink() }
        activity.findViewById<TextView>(R.id.unlink_devices_description).text =
            activity.getString(R.string.login_device_limit_unregister_devices_message_dynamic, maxDevices)
    }

    override fun showDevices(devices: List<Device>) {
        deviceAdapter.addAll(devices)
        updateState()
    }

    private fun updateState() {
        val selectCount = deviceAdapter.objects.filter { it.selected }.size
        
        
        val enableUnlink = deviceAdapter.size() - selectCount <= maxDevices - 1
        unlinkButton.isEnabled = enableUnlink
    }
}