package com.dashlane.autofill.viewallaccounts.view

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.dashlane.autofill.api.databinding.ViewAllItemsDialogFragmentBinding
import com.dashlane.autofill.navigation.getAutofillBottomSheetNavigator
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.DeviceUtils
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.launch

class ViewAllItemsViewProxy(
    private val fragment: ViewAllItemsDialogFragment,
    private val binding: ViewAllItemsDialogFragmentBinding,
    private val authentifiantSearchViewTypeProviderFactory: AuthentifiantSearchViewTypeProviderFactory,
    private val viewModel: ViewAllItemsViewModel
) {
    private val heightPeekExpanded: Pair<Int?, Int?>
    private val context = binding.root.context

    private val authentifiantClickListener =
        EfficientAdapter.OnItemClickListener<DashlaneRecyclerAdapter.ViewTypeProvider> { _, _, item, _ ->
            item?.let {
                it as AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem
                viewModel.selectedCredential(it)
            }
        }
    private val adapter = DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>()

    init {
        adapter.onItemClickListener = authentifiantClickListener
        binding.listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_DRAGGING) {
                    DeviceUtils.hideKeyboard(recyclerView)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                
            }
        })

        heightPeekExpanded = getHeightForPeek() to getHeightForExpanded()

        setupViewHeight()

        val linearLayoutManager = GridLayoutManager(context, 1)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.listview.layoutManager = linearLayoutManager
        binding.listview.adapter = adapter
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                viewModel.filterCredentials(text?.toString() ?: "")
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }
        })
        binding.fab.setOnClickListener {
            val bottomSheetNavigator = fragment.getAutofillBottomSheetNavigator()
            getBottomSheetAuthentifiantListDialogResponse()?.onNavigateToCreateAuthentifiant(
                bottomSheetNavigator
            )
        }

        fragment.lifecycle.coroutineScope.launch {
            fragment.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { state ->
                    if (state is ViewAllItemsState.Loading) {
                        showLoading()
                    } else {
                        hideLoading()
                    }
                    when (state) {
                        is ViewAllItemsState.Loaded -> onUpdateAuthentifiants(state.data, state.query)
                        is ViewAllItemsState.Selected -> onSelected(state.selectedCredential, state.itemListContext)
                        is ViewAllItemsState.Error -> onError()
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupViewHeight() {
        heightPeekExpanded.second?.let { eh ->
            val layoutParams = ConstraintLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, eh)
            binding.root.layoutParams = layoutParams
        }
    }

    private fun getHeightForExpanded(): Int? {
        return context.resources?.displayMetrics?.heightPixels?.let { (it / 4) * 3 }
    }

    private fun getHeightForPeek(): Int? {
        return context.resources?.displayMetrics?.heightPixels?.let { (it / 3) * 1 }
    }

    fun loadContent() {
        viewModel.filterCredentials(getQuery())
    }

    private fun populateItems(searchResult: List<MatchedSearchResult>, query: String?) {
        val totalCount = searchResult.size
        val baseListContext = ItemListContext(
            container = ItemListContext.Container.SEARCH,
            section = ItemListContext.Section.SEARCH_RESULT
        )
        adapter.populateItems(
            searchResult.mapIndexed { index, item ->
                authentifiantSearchViewTypeProviderFactory.create(
                    item,
                    baseListContext.copy(position = index, count = totalCount),
                    query
                )
            }
        )
        binding.listview.scrollToPosition(0)
    }

    fun onNothingSelected() {
        getBottomSheetAuthentifiantListDialogResponse()?.onAuthentifiantDialogResponse(
            authentifiant = null,
            itemListContext = ItemListContext(sectionCount = adapter.itemCount),
            searchQuery = getQuery()
        )
    }

    private fun onSelected(
        authentifiant: VaultItem<SyncObject.Authentifiant>,
        itemListContext: ItemListContext
    ) {
        getBottomSheetAuthentifiantListDialogResponse()?.onAuthentifiantDialogResponse(
            authentifiant,
            itemListContext,
            searchQuery = getQuery()
        )
    }

    private fun getBottomSheetAuthentifiantListDialogResponse(): SearchAuthentifiantDialogResponse? {
        return fragment.activity as? SearchAuthentifiantDialogResponse
    }

    private fun getQuery(): String {
        return binding.search.text?.toString() ?: ""
    }

    private fun onUpdateAuthentifiants(authentifiants: List<MatchedSearchResult>, query: String?) {
        populateItems(authentifiants, query)
    }

    private fun onError() {
        val authentifiants: List<MatchedSearchResult> = emptyList()
        populateItems(authentifiants, null)
    }

    private fun showLoading() {
        binding.listview.visibility = View.INVISIBLE
        binding.viewProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.listview.visibility = View.VISIBLE
        binding.viewProgress.visibility = View.GONE
    }
}
