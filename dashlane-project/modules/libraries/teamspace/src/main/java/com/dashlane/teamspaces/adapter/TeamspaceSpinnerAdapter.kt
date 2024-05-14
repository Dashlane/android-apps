package com.dashlane.teamspaces.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.DimenRes
import com.dashlane.teamspaces.R
import com.dashlane.teamspaces.adapter.TeamspaceDrawableProvider.getIcon
import com.dashlane.teamspaces.model.SpaceName
import com.dashlane.teamspaces.model.TeamSpace

class TeamspaceSpinnerAdapter(context: Context, list: List<TeamSpace>) : ArrayAdapter<TeamSpace>(context, R.layout.spinner_item_dropdown, list) {

    var isDisabled = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent, R.layout.spinner_item_dropdown, R.dimen.teamspace_icon_size_normal)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = getView(position, convertView, parent, R.layout.spinner_item_preview, R.dimen.teamspace_icon_size_edit_selector)
        view.isEnabled = isDisabled
        return view
    }

    private fun getView(position: Int, convertView: View?, parent: ViewGroup, layoutResId: Int, @DimenRes iconSizeRes: Int): View {
        val resources = parent.resources
        val view: TextView
        if (convertView is TextView) {
            view = convertView
        } else {
            view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false) as TextView
            val padding = Math.round(resources.getDimension(R.dimen.spacing_small))
            view.compoundDrawablePadding = padding
        }
        val teamspace = getItem(position) ?: return view
        view.text = teamspace.let { space ->
            when (val name = space.name) {
                is SpaceName.TeamName -> name.value
                is SpaceName.FixName -> context.getString(name.nameRes)
            }
        }
        val drawable = getIcon(view.context, teamspace, iconSizeRes)
        view.setCompoundDrawables(drawable, null, null, null)
        return view
    }
}
