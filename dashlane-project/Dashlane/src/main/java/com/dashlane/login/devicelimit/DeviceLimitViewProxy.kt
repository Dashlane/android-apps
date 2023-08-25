package com.dashlane.login.devicelimit

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.ui.widgets.view.Infobox

class DeviceLimitViewProxy(val activity: FragmentActivity, maxDevices: Int) : DeviceLimitContract.ViewProxy {

    var presenter: DeviceLimitContract.Presenter? = null

    init {
        activity.findViewById<TextView>(R.id.text_title).text =
            activity.getString(R.string.login_device_limit_title_dynamic, maxDevices)
        activity.findViewById<TextView>(R.id.text_subtitle).setText(R.string.login_device_limit_subtitle)

        activity.findViewById<Button>(R.id.negative_button).apply {
            setText(R.string.login_device_limit_unlink_previous_device)
            setOnClickListener {
                presenter?.onUnlinkPreviousDevices()
            }
        }
        activity.findViewById<Button>(R.id.top_left_button).apply {
            setText(R.string.login_device_limit_log_out)
            setOnClickListener {
                presenter?.onLogOut()
            }
        }
        activity.findViewById<Button>(R.id.positive_button).apply {
            setText(R.string.login_device_limit_see_premium_plan)
            setOnClickListener {
                presenter?.onUpgradePremium()
            }
        }
        activity.findViewById<Infobox>(R.id.warning_infobox).apply {
            text = activity.getString(R.string.login_device_limit_tip)
            visibility = View.VISIBLE
        }
    }
}