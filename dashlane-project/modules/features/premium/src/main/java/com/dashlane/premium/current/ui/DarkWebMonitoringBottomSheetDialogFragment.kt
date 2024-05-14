package com.dashlane.premium.current.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.premium.R
import com.dashlane.ui.ExpandedBottomSheetDialogFragment

internal class DarkWebMonitoringBottomSheetDialogFragment : ExpandedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.bottom_sheet_more_info_dark_web_monitoring,
            container,
            false
        )
    }

    companion object {
        const val DIALOG_TAG = "more_info_on_dark_web_monitoring_benefit"

        internal fun newInstance() = DarkWebMonitoringBottomSheetDialogFragment()
    }
}