package com.dashlane.autofill.api.viewallaccounts.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsComponent
import com.dashlane.autofill.api.viewallaccounts.dagger.DaggerBottomSheetViewAllItemsComponent
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.setCurrentPageView
import javax.inject.Inject



class BottomSheetAuthentifiantsSearchAndFilterDialogFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    Fragment() {

    @Inject
    lateinit var viewProxy: BottomSheetAuthentifiantsSearchAndFilterViewProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerBottomSheetViewAllItemsComponent.factory()
            .create(
                AutofillApiViewAllAccountsComponent(requireContext()),
                this
            )
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setCurrentPageView(AnyPage.AUTOFILL_EXPLORE_PASSWORDS, fromAutofill = true)
        return viewProxy.createView(inflater, container)
    }

    override fun onResume() {
        super.onResume()
        viewProxy.loadContent()
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        viewProxy.cancel()
    }
}
