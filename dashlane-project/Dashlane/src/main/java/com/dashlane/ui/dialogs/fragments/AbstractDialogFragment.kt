package com.dashlane.ui.dialogs.fragments

import android.os.Bundle
import com.dashlane.ui.fragments.BaseDialogFragment

@SuppressWarnings("kotlin:S1874")
abstract class AbstractDialogFragment : BaseDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onDestroyView() {
        dialog?.setDismissMessage(null)
        super.onDestroyView()
    }
}