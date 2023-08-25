package com.dashlane.autofill.api.pause.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.util.Toaster
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetAskPauseDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewProxy: BottomSheetAskPauseViewProxy

    @Inject
    lateinit var presenter: AskPauseContract.Presenter

    @Inject
    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openInDashlane = arguments?.getBoolean(EXTRA_OPEN_IN_DASHLANE, false) ?: false
        viewProxy = BottomSheetAskPauseViewProxy(this, presenter, toaster, openInDashlane)
        presenter.setView(viewProxy)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewProxy.onDialogCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
