package com.dashlane.ui.adapters.viewedit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getLabelId
import com.dashlane.xml.domain.SyncObject

class SecureNoteColorArrayAdapter(context: Context, objects: List<SyncObject.SecureNoteType>, selectedPos: Int) :
    ColorSelectionAdapter<SyncObject.SecureNoteType>(context, objects, selectedPos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getViewWithIconAndLabel(
            position,
            convertView,
            parent,
            mData[position].getColorId(),
            mData[position].getLabelId()
        )
    }
}