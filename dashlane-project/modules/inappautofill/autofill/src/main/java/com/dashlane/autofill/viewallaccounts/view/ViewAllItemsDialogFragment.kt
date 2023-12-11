package com.dashlane.autofill.viewallaccounts.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.autofill.api.databinding.ViewAllItemsDialogFragmentBinding
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.setCurrentPageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewAllItemsDialogFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    BottomSheetDialogFragment() {

    private val viewModel: ViewAllItemsViewModel by viewModels()

    @Inject
    lateinit var viewFactory: AuthentifiantSearchViewTypeProviderFactory

    private lateinit var viewProxy: ViewAllItemsViewProxy
    private lateinit var binding: ViewAllItemsDialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setCurrentPageView(AnyPage.AUTOFILL_EXPLORE_PASSWORDS, fromAutofill = true)
        binding = ViewAllItemsDialogFragmentBinding.inflate(inflater, container, false)
        viewProxy = ViewAllItemsViewProxy(this, binding, viewFactory, viewModel)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewProxy.loadContent()
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        viewProxy.onNothingSelected()
    }
}
