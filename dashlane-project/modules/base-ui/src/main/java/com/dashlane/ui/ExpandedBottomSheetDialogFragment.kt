package com.dashlane.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment



open class ExpandedBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog.apply {
            setCancelable(true)
            setOnShowListener {
                findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundResource(R.drawable.bottom_sheet_top_rounded_background)
            }
        }
    }
}