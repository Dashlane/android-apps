package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.wrapper.toItemWrapper
import com.dashlane.ui.activities.fragments.vault.provider.FirstLetterHeaderProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.util.populateItemsAsync
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView
import com.dashlane.ui.widgets.view.empty.SharingItemSelectionEmptyScreen
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SharingNewShareItemViewProxy(
    private val lifecycle: Lifecycle,
    view: View,
    private val viewModel: NewShareItemViewModelContract,
    private val dataType: SyncObjectType
) {
    private val context: Context = view.context
    private val list = view.findViewById<MultiColumnRecyclerView>(R.id.recyclerview)

    private val loadingView = view.findViewById<View>(R.id.data_list_loading)
    private val coroutineScope: CoroutineScope
        get() = lifecycle.coroutineScope

    init {
        view.findViewById<View>(R.id.data_list_floating_button).visibility = View.GONE
        view.findViewById<SwipeRefreshLayout>(R.id.refreshable_layout).isEnabled = false

        coroutineScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is NewShareItemViewModelContract.UIState.Loading -> {
                        loadingView.visibility = View.VISIBLE
                        list.visibility = View.GONE
                    }
                    is NewShareItemViewModelContract.UIState.Data -> {
                        val data =
                            if (dataType == SyncObjectType.AUTHENTIFIANT) {
                                state.list.filterIsInstance<SummaryObject.Authentifiant>()
                            } else {
                                state.list.filterIsInstance<SummaryObject.SecureNote>()
                            }
                        if (data.isEmpty()) {
                            displayEmptyView(state.query)
                        } else {
                            displayList(data)
                        }
                        loadingView.visibility = View.GONE
                        list.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun displayEmptyView(query: String?) {
        list.adapter?.apply {
            clear()
            add(
                SharingItemSelectionEmptyScreen.newInstance(
                    context,
                    dataType,
                    query.isNotSemanticallyNull()
                )
            )
        }
    }

    private suspend fun displayList(data: List<SummaryObject>) {
        val vaultItemsList =
            mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        var lastHeader: String? = null

        data.forEach { summaryObject ->
            val selected = summaryObject.id in viewModel.selectionState.value.secureNotesToShare ||
                    summaryObject.id in viewModel.selectionState.value.accountsToShare

            val viewTypeProvider =
                summaryObject.toItemWrapperSelectable(
                    ItemListContext.Container.NONE.asListContext(),
                    selected
                )
            viewTypeProvider ?: return@forEach
            lastHeader = vaultItemsList.addHeaderIfNeeded(
                context,
                lastHeader,
                viewTypeProvider
            )
            vaultItemsList.add(viewTypeProvider)
        }
        list.adapter?.apply {
            populateItemsAsync(vaultItemsList)
            onItemClickListener =
                EfficientAdapter.OnItemClickListener { _, view, item, _ ->
                    if (item is ItemWrapperSelectable<*>) {
                        val tb =
                            view.findViewById<CompoundButton>(R.id.checkbox).also { it.toggle() }
                        val vaultItem: SummaryObject = item.itemObject

                        val uid = vaultItem.id
                        if (tb.isChecked) {
                            viewModel.onItemSelected(uid, dataType)
                        } else {
                            viewModel.onItemUnSelected(uid, dataType)
                        }
                        item.isSelect = tb.isChecked
                    }
                }
        }
    }

    private fun MutableList<DashlaneRecyclerAdapter.ViewTypeProvider>.addHeaderIfNeeded(
        context: Context,
        lastHeader: String?,
        item: DashlaneRecyclerAdapter.ViewTypeProvider
    ): String {
        val newHeader = FirstLetterHeaderProvider.getHeaderFor(context, item)
        if (lastHeader == null || lastHeader != newHeader) {
            add(HeaderItem(newHeader))
        }
        return newHeader
    }

    private fun SummaryObject.toItemWrapperSelectable(
        container: ItemListContext,
        isSelected: Boolean
    ): DashlaneRecyclerAdapter.ViewTypeProvider? =
        this.toItemWrapper(container)?.let { vault ->
            ItemWrapperSelectable(vault).also { it.isSelect = isSelected }
        }
}