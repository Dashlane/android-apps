package com.dashlane.premium.offer.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.AnnouncementTags
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OfferDetailsFragment : Fragment() {
    private val viewModel by viewModels<OfferDetailsViewModel>()

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.launch {
            viewModel.currentPageViewFlow.collect { page ->
                setCurrentPageView(page)
            }
        }
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    OfferDetailsScreen()
                }
            }
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
}
