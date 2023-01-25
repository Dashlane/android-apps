package com.dashlane.login.monobucket

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.dashlane.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MonobucketUnregisterDeviceFragment : BottomSheetDialogFragment() {
    private val viewModel: MonobucketViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_monobucket_unregister_device, container, false)
        viewModel.monobucketOwner?.let {
            view.findViewById<ImageView>(R.id.device_image).setImageResource(it.iconResId)
            view.findViewById<TextView>(R.id.device_title).text = it.name
            view.findViewById<TextView>(R.id.device_subtitle).text =
                requireContext().getString(
                    R.string.login_monobucket_unregister_device_date,
                    DateUtils.getRelativeTimeSpanString(it.lastActivityDate)
                )
        }

        view.findViewById<View>(R.id.device_unlink).setOnClickListener {
            viewModel.onConfirmUnregisterDevice()
        }
        view.findViewById<View>(R.id.device_cancel).setOnClickListener {
            viewModel.onCancelUnregisterDevice()
            dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!
            val behavior = BottomSheetBehavior.from<View>(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.onCancelUnregisterDevice()
        super.onCancel(dialog)
    }
}