package com.dashlane.item.linkedwebsites.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LinkedWebsitesItemFragment : AbstractContentFragment() {

    private val viewModel by activityViewModels<LinkedWebsitesViewModel>()
    private val websiteAdapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        recyclerView = RecyclerView(requireContext())
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = websiteAdapter
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    websiteAdapter.populateItems(it.viewProvider)
                    it.actionOpenWebsite?.let { opener ->
                        LoginOpener(requireActivity()).show(opener.url, opener.packageNames, null)
                        viewModel.websiteOpened()
                    }
                }
            }
        }
    }
}