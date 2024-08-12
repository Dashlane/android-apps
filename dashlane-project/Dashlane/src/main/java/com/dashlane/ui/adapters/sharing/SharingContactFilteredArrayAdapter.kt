package com.dashlane.ui.adapters.sharing

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.drawable.CircleFirstLetterDrawable
import com.dashlane.ui.drawable.ContactDrawable
import com.dashlane.ui.screens.sharing.SharingContact
import com.tokenautocomplete.FilteredArrayAdapter

class SharingContactFilteredArrayAdapter(
    context: Context,
    private val layoutRes: Int,
    objects: List<SharingContact>
) : FilteredArrayAdapter<SharingContact>(context, layoutRes, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: newView()
        val holder = view.getTag(R.id.view_holder_pattern_tag) as ViewHolder
        val sharingContact = getItem(position) ?: return view
        holder.line1.text = sharingContact.displayName
        holder.icon.setImageDrawable(sharingContact.getDrawable())
        return view
    }

    override fun keepObject(obj: SharingContact, mask: String?): Boolean {
        return mask?.let { obj.displayName.contains(it, ignoreCase = true) } ?: false
    }

    private fun newView(): View {
        val v = LayoutInflater.from(context).inflate(layoutRes, null, false)
        v.setTag(R.id.view_holder_pattern_tag, ViewHolder(v))
        return v
    }

    private data class ViewHolder(private val view: View) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val line1: TextView = view.findViewById(R.id.item_line1)
    }

    private val SharingContact.displayName: String
        get() = when (this) {
            is SharingContact.SharingContactUser -> name
            is SharingContact.SharingContactUserGroup -> name
        }

    private fun SharingContact.getDrawable(): Drawable {
        return when (this) {
            is SharingContact.SharingContactUser ->
                ContactDrawable.newInstance(context, displayName)
            is SharingContact.SharingContactUserGroup ->
                CircleFirstLetterDrawable.newInstance(context, displayName)
        }
    }
}