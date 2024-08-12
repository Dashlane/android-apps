package com.dashlane.ui.activities.fragments.vault.list

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.databinding.FragmentVaultListBinding
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.vault.VaultItemViewTypeProvider
import com.dashlane.ui.activities.fragments.vault.VaultViewModel
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.widgets.view.empty.EmptyScreenViewProvider
import com.dashlane.util.getThemeAttrColor
import com.dashlane.vault.VaultItemLogClickListener
import com.dashlane.vault.VaultItemLogger
import com.dashlane.home.vaultlist.Filter
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.launch

class VaultListViewProxy(
    vaultViewModel: VaultViewModel,
    vaultItemLogger: VaultItemLogger,
    private val vaultListViewModel: VaultListViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val binding: FragmentVaultListBinding,
    private val navigator: Navigator
) : EfficientAdapter.OnItemClickListener<DashlaneRecyclerAdapter.ViewTypeProvider>,
    EfficientAdapter.OnItemLongClickListener<DashlaneRecyclerAdapter.ViewTypeProvider> {
    private val context
        get() = binding.root.context

    private val vaultItemLogClickListener = VaultItemLogClickListener(
        vaultItemLogger,
        this
    ) { (it as? VaultItemViewTypeProvider)?.itemWrapper }

    init {
        setupRefreshLayout()
        binding.dashboardView.adapter?.onItemClickListener = vaultItemLogClickListener
        binding.dashboardView.adapter?.onItemLongClickListener = this

        vaultViewModel.observer(lifecycleOwner) {
            vaultListViewModel.refreshItemList(it)
        }

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    vaultListViewModel.vaultListStateFlow.collect {
                        updateVaultListStateUI(it)
                    }
                }

                launch {
                    vaultListViewModel.scrollToTopFlow.collect {
                        scrollToTop()
                    }
                }
            }
        }
    }

    override fun onItemClick(
        adapter: EfficientAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>,
        view: View,
        item: DashlaneRecyclerAdapter.ViewTypeProvider?,
        position: Int
    ) {
        if (item is VaultItemViewTypeProvider) {
            navigator.goToItem(item.summaryObject.id, item.summaryObject.syncObjectType.xmlObjectName)
        }
    }

    override fun onLongItemClick(
        adapter: EfficientAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>,
        view: View,
        item: DashlaneRecyclerAdapter.ViewTypeProvider?,
        position: Int
    ) {
        if (item is VaultItemViewTypeProvider) {
            navigator.goToQuickActions(item.summaryObject.id, item.itemListContext)
        }
    }

    private fun updateVaultListStateUI(state: VaultListState) {
        updateLoadingState(state.data.isLoading)
        when (state) {
            is VaultListState.EmptyInfo -> {
                binding.dashboardView.adapter?.clear()
                if (state.displayEmptyInfo) {
                    binding.dashboardView.adapter?.populateItems(listOf(getEmptyScreenProvider(state.data.filter)))
                }
            }
            is VaultListState.ItemList -> {
                lifecycleOwner.lifecycleScope.launch {
                    binding.dashboardView.adapter?.populateItemsAsync(state.data.list)
                }
            }
            else -> Unit
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) showLoadingView() else hideLoadingView()
    }

    private fun showLoadingView() {
        binding.loadingView.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.loadingView.visibility = View.GONE
        binding.refreshableLayout.isRefreshing = false
    }

    private fun scrollToTop() {
        binding.dashboardView.smoothScrollToPosition(0)
    }

    private fun measureHeight(): Int = binding.dashboardView.height

    private fun setupRefreshLayout() {
        binding.refreshableLayout.setOnRefreshListener {
            vaultListViewModel.onRefresh()
        }
        binding.refreshableLayout.setColorSchemeColors(context.getColor(R.color.text_brand_standard))
        binding.refreshableLayout.setProgressBackgroundColorSchemeColor(
            context.getThemeAttrColor(R.attr.colorBackgroundFloating)
        )
    }

    private fun getEmptyScreenProvider(filter: Filter): EmptyScreenViewProvider {
        val shouldAlignTop = measureHeight() < context.resources.getDimension(com.dashlane.ui.R.dimen.size_480dp)
        return vaultListViewModel.getEmptyScreenViewProvider(filter, shouldAlignTop)
    }
}