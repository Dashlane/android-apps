package com.dashlane.bottomnavigation

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.dashlane.util.DeviceUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NavigableBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val args: NavigableBottomSheetDialogFragmentArgs by navArgs()
    private val navigationGraphId
        get() = args.navigationGraphId
    private val startDestinationId
        get() = args.startDestinationId
    private val startDestinationArgs
        get() = args.startDestinationArgs
    private val consumeBackPress
        get() = args.consumeBackPress

    private val navHostFragment: NavHostFragment?
        get() = childFragmentManager.findFragmentById(R.id.navigable_bottom_sheet_dialog_nav_host_fragment) as? NavHostFragment
    private val navController: NavController?
        get() = navHostFragment?.navController

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        if (consumeBackPress) {
            
            dialog.apply {
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        navController?.popBackStack() ?: false
                    } else {
                        false
                    }
                }
            }
        }
        dialog.setBottomSheetToStartExpanded()
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflate = inflater.inflate(R.layout.navigable_bottom_sheet_dialog_fragment, container, false)
        val graph = navController?.navInflater?.inflate(navigationGraphId)
        if (startDestinationId != R.id.use_graph_start_destination) {
            graph?.setStartDestination(startDestinationId)
        }
        graph?.let {
            navController?.setGraph(graph, startDestinationArgs)
        }

        return inflate
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        notifyCurrentNavigationOfCancel()
        notifyActivityOfCancel()
    }

    private fun notifyCurrentNavigationOfCancel() {
        val cancelListener =
            getCurrentNavigationFragment() as? NavigableBottomSheetDialogFragmentCanceledListener ?: return
        cancelListener.onNavigableBottomSheetDialogCanceled()
    }

    private fun getCurrentNavigationFragment(): Fragment? {
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    private fun notifyActivityOfCancel() {
        val cancelListener = activity as? NavigableBottomSheetDialogFragmentCanceledListener ?: return
        cancelListener.onNavigableBottomSheetDialogCanceled()
    }

    private fun BottomSheetDialog.setBottomSheetToStartExpanded() {
        this.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog

            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    addBottomSheetCallback(HideKeyboardBottomSheetCallback())
                }
            }
        }
    }

    private class HideKeyboardBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                DeviceUtils.hideKeyboard(bottomSheet)
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            
        }
    }
}