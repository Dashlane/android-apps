package com.dashlane.premium.current.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.dashlane.premium.R
import com.dashlane.premium.current.CurrentPlanLogger
import com.dashlane.ui.ExpandedBottomSheetDialogFragment

internal class DarkWebMonitoringBottomSheetDialogFragment : ExpandedBottomSheetDialogFragment() {

    var logger: CurrentPlanLogger? = null

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

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        logger?.showDwmInfo()
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int =
        super.show(transaction, tag).also {
            logger?.showDwmInfo()
        }

    override fun showNow(manager: FragmentManager, tag: String?) {
        super.showNow(manager, tag)
        logger?.showDwmInfo()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        logger?.closeDwmInfo()
    }

    companion object {
        const val DIALOG_TAG = "more_info_on_dark_web_monitoring_benefit"

        internal fun newInstance() = DarkWebMonitoringBottomSheetDialogFragment()
    }
}