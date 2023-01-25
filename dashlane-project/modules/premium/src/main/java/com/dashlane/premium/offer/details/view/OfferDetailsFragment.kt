package com.dashlane.premium.offer.details.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.AnnouncementTags
import com.dashlane.premium.R
import com.dashlane.premium.offer.details.OfferDetailsViewModel
import com.dashlane.premium.offer.details.OfferDetailsViewProxy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class OfferDetailsFragment : Fragment() {

    private val viewModel by viewModels<OfferDetailsViewModel>()

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_offer_details, null)
        OfferDetailsViewProxy(this, viewModel, view)
        return view
    }

    override fun onStart() {
        super.onStart()
        announcementCenter.fragment(AnnouncementTags.FRAGMENT_PREMIUM)
    }

    override fun onStop() {
        super.onStop()
        announcementCenter.fragment(null)
    }
}
