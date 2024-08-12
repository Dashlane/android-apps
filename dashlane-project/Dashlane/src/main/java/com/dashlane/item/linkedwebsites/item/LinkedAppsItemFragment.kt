package com.dashlane.item.linkedwebsites.item

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dashlane.R
import com.dashlane.databinding.LinkedAppFragmentBinding
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LinkedAppsItemFragment : AbstractContentFragment() {

    private val viewModel: LinkedAppsViewModel by viewModels({ requireParentFragment() })
    private val appsAdapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()
    private lateinit var binding: LinkedAppFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LinkedAppFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.linkedAppsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = appsAdapter
        }
        setEmptyStateDescription()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    appsAdapter.populateItems(it.viewProvider)
                    it.actionOpenApp?.let { packageName ->
                        requireActivity().packageManager.getLaunchIntentForPackage(packageName)
                            ?.let { intent ->
                                startActivity(intent)
                                viewModel.onAppOpened()
                            }
                    }
                    it.actionOpenStore?.let { packageName ->
                        launchPlayStoreApplication(packageName)
                        viewModel.onAppOpened()
                    }
                    it.actionOpenWebsite?.let { opener ->
                        LoginOpener(requireActivity()).show(opener.url, opener.packageNames, null)
                        viewModel.websiteOpened()
                    }
                    binding.groupEmptyState.isVisible = it.showEmptyState
                }
            }
        }
    }

    private fun setEmptyStateDescription() {
        val linkAppString = getString(R.string.autofill_link_app)
        val description = getString(R.string.linked_services_empty_state_description, linkAppString)
        val startIndex = description.indexOf(linkAppString)
        val spannable = SpannableStringBuilder(description)
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + linkAppString.length, 0)
        binding.emptyStateDescription.text = spannable
    }

    private fun launchPlayStoreApplication(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}