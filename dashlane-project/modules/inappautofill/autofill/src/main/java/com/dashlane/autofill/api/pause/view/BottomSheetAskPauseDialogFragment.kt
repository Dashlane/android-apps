package com.dashlane.autofill.api.pause.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.pause.AutofillApiPauseComponent
import com.dashlane.autofill.api.pause.dagger.BottomSheetAskPauseDialogFragmentComponent
import com.dashlane.autofill.api.pause.dagger.DaggerBottomSheetAskPauseDialogFragmentComponent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject



class BottomSheetAskPauseDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewProxy: BottomSheetAskPauseViewProxy

    private lateinit var activityComponent: BottomSheetAskPauseDialogFragmentComponent

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).also {
        initActivityComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewProxy.onDialogCreated()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return viewProxy.createView(inflater, container)
    }

    override fun onResume() {
        super.onResume()
        viewProxy.onResume()
    }

    override fun onCancel(dialog: DialogInterface) {
        viewProxy.onCancel()
        super.onCancel(dialog)
    }

    private fun initActivityComponent() {
        val application = this.activity?.application ?: return

        val openInDashlane = openInDashlane()

        val viewModel = ViewModelProvider(
            this,
            AutofillPauseViewModel.Factory(application, openInDashlane)
        ).get(AutofillPauseViewModel::class.java)

        activityComponent = DaggerBottomSheetAskPauseDialogFragmentComponent.factory()
            .create(
                AutofillApiComponent(application),
                AutofillApiPauseComponent(application),
                viewModel.component,
                this,
                openInDashlane
            )
        activityComponent.inject(this)
    }

    private fun openInDashlane(): Boolean {
        return arguments?.getBoolean(EXTRA_OPEN_IN_DASHLANE, false) ?: false
    }

    companion object {
        const val PAUSE_DIALOG_TAG = "pause_dialog"

        

        private const val EXTRA_OPEN_IN_DASHLANE = "extra_open_in_dashlane"

        internal fun buildFragment(openInDashlane: Boolean = false): BottomSheetAskPauseDialogFragment {
            return BottomSheetAskPauseDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(EXTRA_OPEN_IN_DASHLANE, openInDashlane)
                }
            }
        }
    }
}
