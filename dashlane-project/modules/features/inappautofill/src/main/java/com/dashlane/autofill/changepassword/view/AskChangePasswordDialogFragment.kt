package com.dashlane.autofill.changepassword.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.dashlane.autofill.api.R
import com.dashlane.ui.configureBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AskChangePasswordDialogFragment(private val username: String, private val listener: Actions) :
    BottomSheetDialogFragment() {

    interface Actions {
        fun changePassword()
        fun dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.ask_change_password_bottom_sheet_dialog, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val positiveButton = view.findViewById<Button>(R.id.change_password_button)
        val negativeButton = view.findViewById<Button>(R.id.cancel_button)
        view.findViewById<TextView>(R.id.dialog_email).text = username
        positiveButton.setOnClickListener {
            listener.changePassword()
            dismiss()
        }
        negativeButton.setOnClickListener {
            listener.dismiss()
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).configureBottomSheet()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.dismiss()
    }
}
