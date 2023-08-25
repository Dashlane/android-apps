package com.dashlane.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import com.dashlane.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetListDialogFragment : BottomSheetDialogFragment() {
    var adapter: BaseAdapter? = null
    var onItemClickListener: AdapterView.OnItemClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val contentView = View.inflate(context, R.layout.bottom_sheet_list_dialog_fragment, null)
        val listView = contentView.findViewById(R.id.bottom_sheet_listview) as ListView
        if (adapter != null) {
            listView.adapter = adapter
        }
        if (onItemClickListener != null) {
            listView.onItemClickListener = onItemClickListener
        }
        dialog.apply {
            setContentView(contentView)
            setOnShowListener {
                
                
                val parent = contentView.parent as View
                val behavior = BottomSheetBehavior.from(parent)
                behavior.peekHeight = contentView.height / 2
            }
        }
        return dialog
    }
}
