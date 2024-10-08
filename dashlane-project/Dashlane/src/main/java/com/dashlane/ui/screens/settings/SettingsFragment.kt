package com.dashlane.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import java.net.URL

@AndroidEntryPoint
class SettingsFragment : AbstractContentFragment() {
    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val recyclerView = RecyclerView(inflater.context)

        SettingsViewProxy(
            recyclerView,
            { requireActivity().findViewById(R.id.toolbar) },
            viewModel,
            viewLifecycleOwner.lifecycle,
            onOpenSocialMediaLink = ::openSocialMediaLink,
        )

        setCurrentPageView(viewModel.settingScreenItem.page)

        return recyclerView
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()
    }

    private fun openSocialMediaLink(url: URL) {
        LoginOpener(this.requireActivity()).show(
            url = url.toString(),
            packageNames = setOf(),
            listener = null
        )
    }
}
