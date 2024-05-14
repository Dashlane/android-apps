package com.dashlane.ui.screens.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierDeletedEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.activities.fragments.vault.Filter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import com.dashlane.ui.adapters.text.factory.SearchListTextResolver
import com.dashlane.ui.fab.FabDef
import com.dashlane.ui.fab.FabPresenter
import com.dashlane.ui.screens.fragments.search.ui.SearchListViewHelper
import com.dashlane.ui.screens.settings.SearchableSettingInRecyclerView
import com.dashlane.util.DeviceUtils.hideKeyboard
import com.dashlane.util.animation.fadeIn
import com.dashlane.util.animation.fadeOut
import com.dashlane.util.announceForAccessibility
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.tryOrNull
import com.dashlane.vault.VaultItemLogClickListener
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.valueOfFromDataIdentifier
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment :
    AbstractContentFragment(),
    EfficientAdapter.OnItemClickListener<ViewTypeProvider>,
    EfficientAdapter.OnItemLongClickListener<ViewTypeProvider> {

    @Inject
    lateinit var searchService: SearchService

    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var appEvents: AppEvents

    @Inject
    lateinit var searchListTextResolver: SearchListTextResolver

    @Inject
    lateinit var quickActionProvider: QuickActionProvider

    @Inject
    lateinit var itemWrapperProvider: ItemWrapperProvider

    private val searchViewModel: SearchViewModel by viewModels()
    private val searchLogger: SearchLogger
        by lazy {
            SearchLogger(logRepository)
        }

    private val adapter = DashlaneRecyclerAdapter<ViewTypeProvider>()
    private var searchResultView: RecyclerView? = null
    private var loadingView: View? = null
    private var emptyViewText: TextView? = null
    private var emptyViewIcon: ImageView? = null
    private var fab: ExtendedFloatingActionButton? = null
    private var fabPresenter: FabDef.IPresenter? = null
    private var accessibilityJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setCurrentPageView(AnyPage.SEARCH)
        val layout = inflater.inflate(R.layout.fragment_search, container, false)
        searchResultView = layout.findViewById(R.id.search_result_view)
        loadingView = layout.findViewById(R.id.loading_view)
        emptyViewText = layout.findViewById(R.id.empty_result_message)
        emptyViewText?.text = requireContext().getText(R.string.search_screen_empty_state_body)
        emptyViewIcon = layout.findViewById(R.id.empty_result_icon)
        fab = layout.findViewById(R.id.data_list_floating_button)

        searchResultView?.adapter = adapter
        adapter.onItemClickListener = VaultItemLogClickListener(
            searchService.getVaultItemLogger(),
            this
        ) { item: Any? -> item as? VaultItemWrapper<*> }
        adapter.onItemLongClickListener = this
        setHasOptionsMenu(true)

        fabPresenter = FabPresenter(navigator).also {
            val fabViewProxy = searchService.provideFabViewProxy(layout)
            it.setView(fabViewProxy)
            fabViewProxy.setFilter(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)
        }
        appEvents.register<DataIdentifierDeletedEvent>(this) {
            searchViewModel.repeatLastSearch()
        }
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabPresenter?.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        
        searchViewModel.latestSearchResult.observe(this) { searchResult ->
            onLoaded(searchResult)
        }
        searchViewModel.latestSearchResult.value?.let {
            searchViewModel.searchFromQuery(it.searchRequest)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        tryOrNull {
            val item = menu.findItem(R.id.action_search)
            searchView = (item.actionView as SearchView).apply {
                imeOptions =
                    EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                setOnCloseListener(this@SearchFragment)
                isIconified = false
                maxWidth = android.R.attr.maxWidth
                setQuery(searchViewModel.latestQuery ?: "", true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeSearchView()
        fabPresenter?.onDestroyView()
        appEvents.unregister<DataIdentifierDeletedEvent>(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchLogger.logClose(
            typedCharCount = searchViewModel.latestQuery.charCount(),
            resultCount = searchViewModel.resultCount
        )
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (!isAdded || !isVisible || isRemoving) {
            return false
        }
        loadingView?.visibility = View.VISIBLE
        val request = if (newText.isEmpty()) {
            searchService.getDefaultSearchRequest()
        } else {
            SearchRequest.FromQuery(newText)
        }
        searchViewModel.searchFromQuery(request)

        searchService.refreshLastActionTimeStamp()
        return true
    }

    override fun onClick(v: View) {
        if (v.id == R.id.ID_SEARCH_VIEW) {
            return
        }
        super.onClick(v)
    }

    override fun onItemClick(
        adapter: EfficientAdapter<ViewTypeProvider>,
        view: View,
        item: ViewTypeProvider?,
        position: Int
    ) {
        if (item is VaultItemWrapper<*>) {
            onVaultItemClick(item.summaryObject)
        } else if (item is SearchableSettingInRecyclerView) {
            onSettingItemClick(item)
        }
    }

    override fun onLongItemClick(
        adapter: EfficientAdapter<ViewTypeProvider>,
        view: View,
        item: ViewTypeProvider?,
        position: Int
    ) {
        if (item is VaultItemWrapper<*>) {
            searchService.navigateToQuickAction(item.summaryObject, item.itemListContext)
        }
    }

    override fun onClose(): Boolean {
        hideKeyboard(searchView)
        searchService.popBackStack()
        return false
    }

    private fun onVaultItemClick(vaultItem: SummaryObject?) {
        if (vaultItem == null) {
            return
        }
        closeSearchView()
        val type = vaultItem.valueOfFromDataIdentifier()
        if (searchViewModel.latestSearchResult.value?.searchRequest !is SearchRequest.DefaultRequest) {
            if (activity != null && type != null) {
                searchLogger.logClick(
                    typedCharCount = searchViewModel.latestQuery.charCount(),
                    resultCount = searchViewModel.resultCount
                )
            }
            searchService.markedItemAsSearched(
                itemId = vaultItem.id,
                syncObjectType = vaultItem.syncObjectType
            )
        }
        searchService.navigateToItem(vaultItem)
    }

    private fun onSettingItemClick(recyclerView: SearchableSettingInRecyclerView) {
        closeSearchView()
        searchService.navigateToSettings(
            settingsId = recyclerView.item.getSettingId(),
            origin = null
        )
    }

    private fun closeSearchView() {
        searchView?.let { searchView ->
            searchView.setOnQueryTextListener(null)
            searchView.setOnSearchClickListener(null)
            searchView.setOnCloseListener(null)
            searchView.setQuery(null, false)
            searchView.clearFocus()
            searchView.isIconified = true
        }
    }

    private fun onLoaded(searchResult: SearchResult?) {
        if (searchResult == null) {
            return
        }
        setList(searchResult.result, searchResult.searchRequest)
    }

    private fun setList(list: List<MatchedSearchResult>, request: SearchRequest) {
        accessibilityJob?.cancel()
        loadingView?.visibility = View.GONE
        if (list.isEmpty()) {
            if (request is SearchRequest.DefaultRequest) {
                adapter.clear()
                showMatchedResults()
            } else {
                showEmptyState()
            }
        } else {
            showMatchedResults()
            adapter.populateItems(
                SearchListViewHelper.getWrappedList(
                    requireContext(),
                    list,
                    request,
                    (request as? SearchRequest.FromQuery)?.query,
                    searchListTextResolver,
                    itemWrapperProvider
                )
            )
            searchResultView?.scrollToPosition(0) 
        }

        
        if (request is SearchRequest.FromQuery && request.query.isNotEmpty()) {
            accessibilityJob = lifecycleScope.launch {
                delay(2000)
                requireContext().announceForAccessibility(
                    resources.getQuantityString(R.plurals.and_accessibility_search_result, list.size, list.size),
                    false
                )
            }
        }
    }

    private fun showEmptyState() {
        searchResultView?.fadeOut()
        emptyViewText?.fadeIn()
        emptyViewIcon?.fadeIn()
        fab?.isEnabled = true
        fab.fadeIn()
    }

    private fun showMatchedResults() {
        searchResultView?.fadeIn()
        emptyViewText?.fadeOut()
        emptyViewIcon?.fadeOut()
        fab?.isEnabled = false
        fab.fadeOut()
    }
}

private fun String?.charCount() = this?.length ?: 0
