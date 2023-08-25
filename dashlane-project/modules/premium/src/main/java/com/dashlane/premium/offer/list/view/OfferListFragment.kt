package com.dashlane.premium.offer.list.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.AnnouncementTags
import com.dashlane.premium.R
import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.premium.offer.common.model.OfferType.valueOf
import com.dashlane.premium.offer.common.model.OffersState
import com.dashlane.premium.offer.list.OfferListContract
import com.dashlane.premium.offer.list.OfferListDataProvider
import com.dashlane.premium.offer.list.OfferListPresenter
import com.dashlane.premium.offer.list.OfferListViewProxy
import com.dashlane.premium.offer.list.view.OfferListFragmentDirections.Companion.goToOffersDetailsFromOffersOverview
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OfferListFragment : Fragment() {

    private val navController: NavController
        get() = findNavController()

    @Inject
    lateinit var logger: OffersLogger

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    @Inject
    internal lateinit var provider: OfferListDataProvider

    @Inject
    internal lateinit var userFeaturesChecker: UserFeaturesChecker

    private var presenter: OfferListContract.Presenter? = null

    private var args: OfferListFragmentArgs? = null
    private var autoRedirectedToOfferDetails = false
    private var origin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreAutoRedirectedToOfferDetails(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_offer_list, container, false)
        val viewModel = ViewModelProvider(this).getDeferredViewModel<OffersState?>(VIEW_MODEL_TAG)
        val extras = requireActivity().intent.extras
        args = if (extras != null) {
            OfferListFragmentArgs.fromBundle(extras)
        } else {
            OfferListFragmentArgs.fromBundle(requireArguments())
        }
        origin = args?.origin

        logger.origin = origin.orEmpty()
        lifecycleScope.launch {
            logger.currentPageViewFlow.collect { (page, fromAutofill) ->
                requireActivity().setCurrentPageView(page, fromAutofill)
            }
        }

        presenter = OfferListPresenter(
            coroutineScope = lifecycleScope,
            viewModel = viewModel,
            navController = navController,
            logger = logger
        ).apply {
            setView(OfferListViewProxy(view))
            setProvider(provider)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val offerType = args?.offerType
        if (!autoRedirectedToOfferDetails && offerType != null) {
            navController.navigate(goToOffersDetailsFromOffersOverview(valueOf(offerType), origin))
            autoRedirectedToOfferDetails = true
        }
    }

    override fun onStart() {
        super.onStart()
        announcementCenter.fragment(AnnouncementTags.FRAGMENT_PREMIUM)
    }

    override fun onStop() {
        super.onStop()
        announcementCenter.fragment(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isFinishing == true) {
            logger.onOfferListFinishing()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AUTO_REDIRECTED_TO_OFFER_DETAILS_KEY, autoRedirectedToOfferDetails)
    }

    private fun restoreAutoRedirectedToOfferDetails(savedInstanceState: Bundle?) {
        if (savedInstanceState?.getBoolean(AUTO_REDIRECTED_TO_OFFER_DETAILS_KEY, false) == true) {
            autoRedirectedToOfferDetails = true
        }
    }

    companion object {
        const val EXTRA_ORIGIN = "origin"
        const val ORIGIN_DEVICE_LIMIT = "device_limit"
        const val ORIGIN_MONOBUCKET = "mono_test"
        const val AUTO_REDIRECTED_TO_OFFER_DETAILS_KEY = "auto_redirected_to_offer_details_key"
        private const val VIEW_MODEL_TAG = "offers_list"

        fun userLockedOut(origin: String?) = ORIGIN_DEVICE_LIMIT == origin || ORIGIN_MONOBUCKET == origin
    }
}
