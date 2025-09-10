package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection.old

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.legacy.recyclerview.DashlaneRecyclerAdapter
import com.dashlane.ui.legacy.recyclerview.HeaderItem
import com.dashlane.ui.legacy.recyclerview.MultiColumnRecyclerView
import com.dashlane.ui.legacy.recyclerview.util.populateItemsAsync
import com.dashlane.ui.widgets.view.empty.SharingItemSelectionEmptyScreen
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SharingNewShareItemViewProxy(
    private val lifecycle: Lifecycle,
    view: View,
    private val viewModel: NewShareItemViewModel,
    private val dataType: SyncObjectType,
    private val itemWrapperProvider: ItemWrapperProvider,
    private val identityNameHolderService: IdentityNameHolderService,
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
            viewModel.uiState.viewState.collect { state ->
                when (state.list) {
                    null -> {
                        loadingView.visibility = View.VISIBLE
                        list.visibility = View.GONE
                    }
                    else -> {
                        val data =
                            if (dataType == SyncObjectType.AUTHENTIFIANT) {
                                state.list.filterIsInstance<SummaryObject.Authentifiant>()
                            } else {
                                state.list.filterIsInstance<SummaryObject.SecureNote>()
                            }
                        if (data.isEmpty()) {
                            displayEmptyView(state.query)
                        } else {
                            displayList(data, state.selectionForSharing.map { it.id })
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
                    query.isNotSemanticallyNull(),
                ),
            )
        }
    }

    private suspend fun displayList(data: List<SummaryObject>, selectionUids: List<String>) {
        val vaultItemsList = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        var lastHeader: String? = null

        data.forEach { summaryObject ->
            val selected = summaryObject.id in selectionUids

            val viewTypeProvider =
                summaryObject.toItemWrapperSelectable(ItemListContext.Container.NONE.asListContext(), selected)
            viewTypeProvider ?: return@forEach
            lastHeader = vaultItemsList.addHeaderIfNeeded(
                context,
                lastHeader,
                viewTypeProvider,
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

                        if (tb.isChecked) {
                            viewModel.onItemSelected(item.summaryObject)
                        } else {
                            viewModel.onItemUnSelected(item.summaryObject)
                        }
                        item.isSelect = tb.isChecked
                    }
                }
        }
    }

    private fun MutableList<DashlaneRecyclerAdapter.ViewTypeProvider>.addHeaderIfNeeded(
        context: Context,
        lastHeader: String?,
        item: DashlaneRecyclerAdapter.ViewTypeProvider,
    ): String {
        val newHeader = FirstLetterHeaderProvider.getHeaderFor(context, item, identityNameHolderService)
        if (lastHeader == null || lastHeader != newHeader) {
            add(HeaderItem(newHeader))
        }
        return newHeader
    }

    private fun SummaryObject.toItemWrapperSelectable(
        container: ItemListContext,
        isSelected: Boolean,
    ): DashlaneRecyclerAdapter.ViewTypeProvider? =
        itemWrapperProvider(this, container)?.let { vault ->
            ItemWrapperSelectable(vault).also { it.isSelect = isSelected }
        }
}