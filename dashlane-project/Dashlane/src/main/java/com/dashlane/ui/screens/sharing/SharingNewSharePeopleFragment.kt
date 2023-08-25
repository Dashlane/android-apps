package com.dashlane.ui.screens.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingNewSharePeopleFragment : AbstractContentFragment() {
    private val viewModel by viewModels<NewSharePeopleViewModel>()
    private lateinit var viewProxy: NewSharePeopleViewProxy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_sharing_message, container, false)
        viewProxy = NewSharePeopleViewProxy(this, view, viewModel)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sharing_share_item_send, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sharing_share_item_send -> {
                viewProxy.onClickShare()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val FROM_ITEM_VIEW = "from_item_view"
    }
}
