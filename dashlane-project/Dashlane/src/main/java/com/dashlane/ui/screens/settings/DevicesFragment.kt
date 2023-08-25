package com.dashlane.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.endpoints.devices.ListDevicesService.Data.Device
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment.TwoButtonClicker
import com.dashlane.util.DeviceListManager
import com.dashlane.util.setCurrentPageView
import com.dashlane.vault.model.DeviceType.Companion.forValue
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

@AndroidEntryPoint
class DevicesFragment : AbstractContentFragment() {
    private lateinit var listView: GridView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLabel: TextView

    lateinit var deviceListManager: DeviceListManager
    private val job: Job = SupervisorJob(null)
    private val deviceList: MutableList<Device> = mutableListOf()
    private var infoDialog: DialogFragment? = null
    private var confirmDeleteDialog: DialogFragment? = null

    @Inject
    lateinit var dashlaneApi: DashlaneApi

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = sessionManager.session
        if (session != null) {
            deviceListManager = DeviceListManager(job, session, dashlaneApi) { devices: List<Device> ->
                deviceList.clear()
                deviceList.addAll(devices)
                setupListView(true)
            }
        } else {
            childFragmentManager.popBackStack()
        }
    }

    override fun onStart() {
        super.onStart()
        refreshDevices()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.setCurrentPageView(AnyPage.SETTINGS_DEVICE_LIST)
        val view = inflater.inflate(R.layout.fragment_devices_list, container, false)
        listView = view.findViewById(R.id.device_list)
        progressBar = view.findViewById(R.id.progressbar)
        progressLabel = view.findViewById(R.id.progressbar_label)
        setupListView(false)

        
        val previousDelete = childFragmentManager
            .findFragmentByTag(FRAGMENT_TAG_CONFIRM_DELETE_DIALOG) as DialogFragment?
        previousDelete?.dismissAllowingStateLoss()
        val previousInfoDialog = childFragmentManager.findFragmentByTag(FRAGMENT_TAG_INFO_DIALOG) as DialogFragment?
        previousInfoDialog?.dismissAllowingStateLoss()
        return view
    }

    private fun showDeviceInfoDialog(device: Device) {
        if (infoDialog != null && infoDialog!!.isVisible) {
            infoDialog!!.dismissAllowingStateLoss()
        }
        infoDialog = setupInfoDialog(device)
        infoDialog?.show(childFragmentManager, FRAGMENT_TAG_INFO_DIALOG)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel(null)
    }

    private fun setupConfirmDeleteDialog(device: Device): DialogFragment {
        val clicker: TwoButtonClicker = object : TwoButtonClicker {
            override fun onPositiveButton() {
                deleteDevice(device)
            }

            override fun onNegativeButton() {}
        }
        return NotificationDialogFragment.Builder().setTitle(getString(R.string.delete_device_dialog_title))
            .setMessage(getString(R.string.delete_device_dialog_body))
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.delete_device))
            .setCancelable(true)
            .setClicker(clicker).setClickPositiveOnCancel(false)
            .build()
    }

    private fun showConfirmDeleteDialog(device: Device) {
        if (confirmDeleteDialog != null && confirmDeleteDialog!!.isVisible) {
            confirmDeleteDialog!!.dismissAllowingStateLoss()
        }
        confirmDeleteDialog = setupConfirmDeleteDialog(device)
        confirmDeleteDialog?.show(childFragmentManager, FRAGMENT_TAG_CONFIRM_DELETE_DIALOG)
    }

    private fun setupInfoDialog(device: Device): DialogFragment {
        val clicker: TwoButtonClicker = object : TwoButtonClicker {
            override fun onPositiveButton() {}
            override fun onNegativeButton() {
                showConfirmDeleteDialog(device)
            }
        }
        val devInfo = StringBuilder()
        devInfo.append("\n")
            .append(getString(R.string.device_name))
            .append("\n")
            .append(device.name)
        val deviceId = SingletonProvider.getSessionManager().session!!.deviceId
        val isCurrentDevice = deviceId == device.id
        if (isCurrentDevice) {
            devInfo.append("\n").append(getString(R.string.device_this_device).uppercase(Locale.getDefault()))
        }
        devInfo.append("\n\n")
            .append(getString(R.string.device_type))
            .append("\n")
            .append(getString(forValue(device.platform).nameResId))
            .append("\n\n")
            .append(getString(R.string.device_created))
            .append("\n")
            .append(device.formattedCreationDate)
            .append("\n\n")
            .append(getString(R.string.device_lastupdate))
            .append("\n")
            .append(device.formattedUpdateDate)
        var deleteDevice: String? = getString(R.string.delete_device)
        if (isCurrentDevice) {
            deleteDevice = null
        }
        return NotificationDialogFragment.Builder().setTitle(getString(R.string.device_dialog_title))
            .setMessage(devInfo.toString())
            .setNegativeButtonText(deleteDevice)
            .setPositiveButtonText(getString(R.string.ok))
            .setCancelable(true).setClicker(clicker)
            .setClickPositiveOnCancel(false).build()
    }

    private fun deleteDevice(d: Device) {
        val deviceListManager = deviceListManager
        deviceListManager.deleteAsync(d) {
            deviceList.remove(d)
            setupListView(true)
            refreshDevices()
            refreshDevices()
        }
    }

    private fun setupListView(result: Boolean) {
        if (result) {
            progressBar.visibility = View.GONE
            if (deviceList.size == 0) {
                progressLabel.text = getText(R.string.loading_devices_error)
            } else {
                progressLabel.visibility = View.GONE
            }
        }
        listView.adapter = DeviceListAdapter(deviceList)
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position: Int, _ ->
            showDeviceInfoDialog(deviceList[position])
        }
    }

    private fun refreshDevices() {
        val deviceListManager = deviceListManager
        deviceListManager.refresh()
    }

    private inner class DeviceListAdapter(deviceList: List<Device>) : BaseAdapter() {
        private val aDeviceList: List<Device>

        init {
            aDeviceList = deviceList
        }

        override fun getCount(): Int {
            return aDeviceList.size
        }

        override fun getItem(position: Int): Device {
            return aDeviceList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = newView()
            }
            bindView(view!!, position)
            return view
        }

        private fun newView(): View? {
            val inflater = LayoutInflater.from(activity)
            return inflater.inflate(R.layout.item_device, null)
        }

        private fun bindView(convertView: View, position: Int) {
            val device: Device = aDeviceList[position]
            val name = convertView.findViewById<TextView>(R.id.device_title)
            val type = convertView.findViewById<TextView>(R.id.device_subtitle)
            val thumb = convertView.findViewById<ImageView>(R.id.device_image)
            name.text = device.name
            val platform = forValue(device.platform)
            type.setText(platform.nameResId)
            thumb.setImageResource(platform.iconResId)
        }
    }

    companion object {
        private const val FRAGMENT_TAG_INFO_DIALOG = "device_info_dialog"
        private const val FRAGMENT_TAG_CONFIRM_DELETE_DIALOG = "device_delete_dialog"
    }
}