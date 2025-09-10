package com.dashlane.ui.screens.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.search.SearchRequest
import com.dashlane.search.SearchScreen
import com.dashlane.search.SearchViewModel
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.DeviceUtils.hideKeyboard
import com.dashlane.util.announceForAccessibility
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.tryOrNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : AbstractContentFragment() {
    private val searchViewModel: SearchViewModel by viewModels()
    private var loadingView: View? = null
    private var accessibilityJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        this.setCurrentPageView(AnyPage.SEARCH)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    SearchScreen(
                        searchViewModel = searchViewModel,
                        fabViewModel = hiltViewModel(),
                        openItemDetails = { itemId, syncObjectType ->
                            navigator.goToItem(
                                uid = itemId,
                                type = syncObjectType.xmlObjectName,
                            )
                        },
                        openQuickActions = { itemId, itemListContext ->
                            navigator.goToQuickActions(
                                itemId = itemId,
                                itemListContext = itemListContext,
                            )
                        },
                        onAddItemClick = { syncObjectType ->
                            navigator.goToCreateItem(syncObjectType.xmlObjectName)
                        },
                        onResultDisplayed = ::onResultDisplayed,
                    )
                }
            }
        }
    }

    private fun onResultDisplayed(request: SearchRequest, size: Int) {
        if (request is SearchRequest.FromQuery && request.query.isNotEmpty()) {
            accessibilityJob = lifecycleScope.launch {
                delay(2_000)
                requireContext().announceForAccessibility(
                    resources.getQuantityString(
                        R.plurals.and_accessibility_search_result,
                        size,
                        size,
                    ),
                    false,
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        @Suppress("DEPRECATION")
        super.onPrepareOptionsMenu(menu)
        tryOrNull {
            val item = menu.findItem(R.id.action_search)
            searchView = (item.actionView as SearchView).apply {
                imeOptions =
                    EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
                setOnCloseListener(this@SearchFragment)
                isIconified = false
                maxWidth = android.R.attr.maxWidth
                val searchRequest = searchViewModel.searchState.viewState.value.searchRequest
                setQuery(
                    (searchRequest as? SearchRequest.FromQuery)?.query ?: "",
                    true,
                )
            }
        }
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (!isAdded || !isVisible || isRemoving) {
            return false
        }
        loadingView?.visibility = View.VISIBLE
        if (newText.isEmpty()) {
            searchViewModel.showRecentSearches()
        } else {
            searchViewModel.searchFromQuery(newText)
        }

        return true
    }

    override fun onClick(v: View) {
        if (v.id == R.id.ID_SEARCH_VIEW) {
            return
        }
        super.onClick(v)
    }

    override fun onClose(): Boolean {
        hideKeyboard(searchView)
        navigator.popBackStack()
        return false
    }
}