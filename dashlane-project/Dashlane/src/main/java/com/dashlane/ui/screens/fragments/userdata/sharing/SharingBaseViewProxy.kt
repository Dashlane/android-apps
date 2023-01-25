package com.dashlane.ui.screens.fragments.userdata.sharing

import android.content.Context
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dashlane.R
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView
import com.dashlane.ui.widgets.view.RecyclerViewFloatingActionButton
import kotlinx.coroutines.CoroutineScope



open class SharingBaseViewProxy(
    private val fragment: Fragment,
    view: View
) {
    val fragmentManager: FragmentManager
        get() = fragment.parentFragmentManager
    val lifecycle: Lifecycle
        get() = fragment.lifecycle
    val coroutineScope: CoroutineScope
        get() = lifecycle.coroutineScope
    val context: Context
        get() = fragment.requireContext()
    val list: MultiColumnRecyclerView = view.findViewById(R.id.recyclerview)
    val loadingView: View = view.findViewById(R.id.data_list_loading)
    val refreshLayout: SwipeRefreshLayout = view.findViewById(R.id.refreshable_layout)
    val fabButton: RecyclerViewFloatingActionButton =
        view.findViewById(R.id.data_list_floating_button)
    val toolbar: Toolbar? = fragment.activity?.findViewById(R.id.toolbar)

    init {
        fabButton.visibility = View.GONE
    }

    fun showLoadingState() {
        loadingView.visibility = View.VISIBLE
        list.visibility = View.INVISIBLE
    }

    fun showEmptyState() {
        loadingView.visibility = View.GONE
        refreshLayout.isRefreshing = false
        list.visibility = View.VISIBLE
    }
}