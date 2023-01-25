package com.dashlane.login.devicelimit

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.R
import com.dashlane.login.Device
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



class DeviceViewHolder(private val v: View) : EfficientViewHolder<Device>(v) {
    val checkBox: CheckBox = view.findViewById(R.id.device_checkbox)
    var itemCheckListener: DeviceAdapter.OnItemCheckListener? = null
    private val icon: ImageView = view.findViewById(R.id.device_image)
    private val name: TextView = view.findViewById(R.id.device_title)
    private val subtitle: TextView = view.findViewById(R.id.device_subtitle)

    override fun updateView(context: Context, item: Device?) {
        item ?: return
        v.isSelected = item.selected
        checkBox.apply {
            setOnCheckedChangeListener(null)
            isChecked = item.selected
            setOnCheckedChangeListener { _, checked ->
                v.isSelected = checked
                item.selected = checked
                itemCheckListener?.onItemCheckChanged(item)
            }
        }
        icon.setImageResource(item.iconResId)
        name.text = item.name
        subtitle.text = v.context.getString(
            R.string.login_device_limit_unregister_device_date,
            DateUtils.getRelativeTimeSpanString(item.lastActivityDate)
        )
    }
}