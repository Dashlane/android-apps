package com.dashlane.ui.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorDashboardFragment
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.vault.VaultFragment
import com.dashlane.ui.fragments.BaseUiFragment
import com.dashlane.ui.screens.fragments.userdata.CredentialAddStep1Fragment
import com.dashlane.util.getColorOnForToolbar
import com.dashlane.util.setMagIconTint
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class AbstractContentFragment :
    BaseUiFragment(),
    View.OnClickListener,
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener {

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var navigator: Navigator

    protected var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu.findItem(R.id.action_search) == null && !excludedFragmentsForMenu.any { it == this::class.java }) {
            inflater.inflate(R.menu.main_menu, menu)
            val searchItem = menu.findItem(R.id.action_search)
            setupSearchView((searchItem.actionView as SearchView?)!!)
        }
    }

    override fun onDestroyView() {
        searchView?.let { searchView ->
            searchView.setOnSearchClickListener(null)
            searchView.setOnQueryTextListener(null)
            searchView.setOnCloseListener(null)
        }
        super.onDestroyView()
    }

    override fun onQueryTextChange(newText: String): Boolean {
        lockManager.setLastActionTimestampToNow()
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return true
    }

    override fun onClick(v: View) {
        if (v.id == R.id.ID_SEARCH_VIEW) {
            navigator.goToSearch(null)
        }
    }

    override fun onClose(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    protected fun setupSearchView(searchView: SearchView) {
        searchView.let {
            searchView.setIconifiedByDefault(true)
            searchView.id = R.id.ID_SEARCH_VIEW
            searchView.setOnSearchClickListener(this)
            searchView.setOnQueryTextListener(this)
            searchView.setOnCloseListener(this)
            searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            searchView.setMagIconTint(
                requireContext().getColorOnForToolbar(
                    requireContext().getColor(R.color.container_agnostic_neutral_standard)
                )
            )
            this.searchView = searchView
        }
    }

    companion object {
        private val excludedFragmentsForMenu = listOf(
            VaultFragment::class.java,
            CredentialAddStep1Fragment::class.java,
            PasswordGeneratorAndGeneratedPasswordFragment::class.java,
            AuthenticatorDashboardFragment::class.java
        )
    }
}