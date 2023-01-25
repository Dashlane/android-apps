package com.dashlane.ui.screens.settings

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



class SettingsHeaderInRecyclerView(private val header: SettingHeader) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(view: View) : EfficientViewHolder<SettingsHeaderInRecyclerView>(view) {

        override fun isClickable() = false

        override fun updateView(context: Context, item: SettingsHeaderInRecyclerView?) {
            (view as TextView).text = item?.header?.title
            ViewCompat.setAccessibilityHeading(view, true)
        }
    }

    companion object {

        val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<SettingsHeaderInRecyclerView> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.item_settings_header, ViewHolder::class.java
            )
    }
}