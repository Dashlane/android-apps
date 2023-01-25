package com.dashlane.ui.adapters.viewedit

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.dashlane.R

open class ColorSelectionAdapter<SyncObject>(context: Context, objects: List<SyncObject>, selectedPos: Int) :
    ArrayAdapter<SyncObject>(context, R.layout.list_item_color, objects) {

    protected val mData: List<SyncObject>
    private val mSelectedPosition: Int

    init {
        mData = objects
        mSelectedPosition = selectedPos
    }

    fun getViewWithIconAndLabel(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        @ColorRes color: Int,
        @StringRes label: Int
    ): View {
        val resultView: View
        val holder: ColorHolder
        if (convertView != null) {
            resultView = convertView
            holder = resultView.getTag(R.id.view_holder_pattern_tag) as ColorHolder
        } else {
            resultView = LayoutInflater.from(context).inflate(R.layout.list_item_color, parent, false)
            holder = ColorHolder(resultView)
            resultView.setTag(R.id.view_holder_pattern_tag, holder)
        }
        if (mSelectedPosition == position) {
            holder.colorCheck.visibility = View.VISIBLE
        } else {
            holder.colorCheck.visibility = View.GONE
        }
        holder.colorName.setText(label)
        holder.colorIcon.setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.MULTIPLY)
        return resultView
    }

    internal data class ColorHolder(val v: View) {
        var colorIcon: ImageView = v.findViewById(R.id.color_icon)
        var colorCheck: ImageView = v.findViewById(R.id.color_check)
        var colorName: TextView = v.findViewById(R.id.color_name)
    }
}