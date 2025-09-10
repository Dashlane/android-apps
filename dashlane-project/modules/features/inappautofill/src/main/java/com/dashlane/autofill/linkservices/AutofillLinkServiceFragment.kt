package com.dashlane.autofill.linkservices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.dashlane.autofill.phishing.AntiPhishingCapabilityChecker
import com.dashlane.autofill.viewallaccounts.view.AutofillViewAllItemsActivity.Companion.LINK_SERVICE_REQUEST_KEY
import com.dashlane.autofill.viewallaccounts.view.AutofillViewAllItemsActivity.Companion.LINK_SERVICE_SHOULD_AUTOFILL
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.design.theme.DashlaneTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillLinkServiceFragment : Fragment(), NavigableBottomSheetDialogFragmentCanceledListener {
    @Inject
    lateinit var antiPhishingCapabilityChecker: AntiPhishingCapabilityChecker
    private val linkServicesNavigationArgs: AutofillLinkServiceFragmentArgs by navArgs<AutofillLinkServiceFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    val startDestination =
                        if (antiPhishingCapabilityChecker.showAntiPhishingWarning(
                                formSource = linkServicesNavigationArgs.formSource,
                                itemWebsite = linkServicesNavigationArgs.itemWebsite,
                            )
                        ) {
                            AutofillLinkServicesDestination.PhishingWarning
                        } else {
                            AutofillLinkServicesDestination.RememberAccount
                        }

                    AutofillLinkServiceNavigation(
                        startDestination = startDestination,
                        finishWithResult = ::finishWithResult,
                        navigationArgs = linkServicesNavigationArgs,
                    )
                }
            }
        }
    }

    private fun finishWithResult(shouldAutofill: Boolean) {
        setFragmentResult(
            LINK_SERVICE_REQUEST_KEY,
            bundleOf(LINK_SERVICE_SHOULD_AUTOFILL to shouldAutofill),
        )
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        
    }
}