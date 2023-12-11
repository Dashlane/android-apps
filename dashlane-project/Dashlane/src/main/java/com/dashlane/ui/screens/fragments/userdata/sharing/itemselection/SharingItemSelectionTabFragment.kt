package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SharingItemSelectionTabFragment : AbstractContentFragment() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel by viewModels<NewShareItemViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setCurrentPageView(AnyPage.SHARING_CREATE_ITEM)
        return inflater.inflate(R.layout.fragment_tablayout_viewpager2_with_fab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        activity?.invalidateOptionsMenu()

        SharingItemSelectionTabViewProxy(view, this, viewModel)
    }

    override fun onQueryTextSubmit(query: String): Boolean = viewModel.onQueryChange(query)

    override fun onQueryTextChange(newText: String): Boolean = viewModel.onQueryChange(newText)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ID_SEARCH_VIEW -> searchView?.setQuery("", false)
            else -> super.onClick(v)
        }
    }

    override fun onClose(): Boolean = viewModel.onQueryChange()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        setupSearchView(searchItem.actionView as SearchView)
        searchView?.queryHint = getString(R.string.search)
    }
}
