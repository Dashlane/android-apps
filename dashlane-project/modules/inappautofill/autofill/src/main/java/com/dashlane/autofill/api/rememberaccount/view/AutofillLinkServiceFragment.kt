package com.dashlane.autofill.api.rememberaccount.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dashlane.autofill.api.R
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillLinkServiceFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    Fragment() {

    private val viewModel by viewModels<AutofillLinkServiceViewModel>()

    @Inject
    lateinit var logger: AutofillLinkServiceLogger

    lateinit var viewProxy: AutofillLinkServiceViewProxy

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_link_service, container, false)
        viewProxy = AutofillLinkServiceViewProxy(this, view, viewModel, logger)
        return view
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        logger.logLinkServiceCancel()
        viewProxy.finishWithResult(shouldAutofill = false, shouldLink = false)
    }
}