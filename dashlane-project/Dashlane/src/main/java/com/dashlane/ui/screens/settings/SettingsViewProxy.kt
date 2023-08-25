package com.dashlane.ui.screens.settings

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.screens.settings.item.SettingChange
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.util.SnackbarUtils
import kotlinx.coroutines.launch

class SettingsViewProxy(
    private val recyclerView: RecyclerView,
    private val toolbarProvider: () -> Toolbar,
    private val viewModel: SettingsViewModelContract,
    private val lifecycle: Lifecycle
) {
    private val settingChangeListener = object : SettingChange.Listener {
        override fun onSettingsInvalidate() {
            refreshUi()
        }
    }

    private val context get() = recyclerView.context

    private val linearLayoutManager = LinearLayoutManager(context)
    private val adapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()

    init {
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncFeedbacks.collect { showSnackbar(it) }
            }
        }

        lifecycle.coroutineScope.launch {
            var wasVisible = false
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.use2faSettingStateChanges.collect { visible ->
                    if (wasVisible != visible) {
                        wasVisible = visible
                        refreshUi()
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                refreshUi()

                toolbarProvider().title = viewModel.settingScreenItem.title
                    .takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.action_bar_settings)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                findFirstVisibleItemPosition().takeIf { it > 0 }
                    ?.let { viewModel.pendingAdapterPosition = it }
            }
        })
    }

    private fun refreshUi() {
        viewModel.onRefresh()

        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        var latestHeader: SettingHeader? = null
        viewModel.settingScreenItem.subItems
            .filter { it.isVisible() }
            .forEach {
                val newHeader = it.header
                if (newHeader?.title != latestHeader?.title) {
                    if (newHeader != null) {
                        items.add(SettingsHeaderInRecyclerView(newHeader))
                    }
                    latestHeader = newHeader
                }
                (it as? SettingChange.Listenable)?.listener = settingChangeListener
                items.add(
                    SettingInRecyclerView(it).apply {
                        onSettingInteraction = { viewModel.onSettingInteraction() }
                    }
                )
            }

        if (viewModel.shouldHighlightSetting) {
            
            viewModel.targetId?.let { targetId ->
                val positionItem = items.indexOfFirst {
                    if ((it as? SettingInRecyclerView)?.display?.id == targetId) {
                        it.needsHighlight = true
                        true
                    } else {
                        false
                    }
                }
                if (positionItem >= 0) {
                    viewModel.pendingAdapterPosition = positionItem
                }
            }
            viewModel.shouldHighlightSetting = false
        }

        setItems(items)
        applyStoredPosition()
    }

    private fun applyStoredPosition() {
        if (viewModel.pendingAdapterPosition < 0) {
            return
        }
        val applied = scrollToAdapterPosition(viewModel.pendingAdapterPosition)
        if (applied) {
            
            viewModel.pendingAdapterPosition = -1
        }
    }

    private fun setItems(items: List<DashlaneRecyclerAdapter.ViewTypeProvider>) {
        val adapterPosition = findFirstVisibleItemPosition()
        adapter.populateItems(items)
        scrollToAdapterPosition(adapterPosition)
    }

    private fun findFirstVisibleItemPosition() = linearLayoutManager.findFirstVisibleItemPosition()

    private fun scrollToAdapterPosition(position: Int): Boolean {
        if (adapter.size() - 1 < position) return false
        recyclerView.scrollToPosition(position)
        return true
    }

    private fun showSnackbar(stringRes: Int) {
        SnackbarUtils.showSnackbar(
            anchorView = recyclerView,
            text = context.resources.getString(stringRes)
        )
    }
}