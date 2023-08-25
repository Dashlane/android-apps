package com.dashlane.autofill.api.unlinkaccount.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.unlinkaccount.UnlinkAccountsContract
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.Toaster
import com.dashlane.vault.summary.SummaryObject

class UnlinkAccountsViewProxy(
    private val fragment: UnlinkAccountsFragment,
    private val presenter: UnlinkAccountsContract.Presenter,
    private val toaster: Toaster,
    private val linkedAccountViewTypeProviderFactory: LinkedAccountViewTypeProviderFactory
) : UnlinkAccountsContract.View {
    private lateinit var progressView: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var itemsListView: RecyclerView
    private lateinit var listAdapter: DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>
    private lateinit var layoutEmptyStateViewGroup: ViewGroup
    private lateinit var layoutErrorStateViewGroup: ViewGroup
    private lateinit var refreshPageButton: Button
    private lateinit var activity: FragmentActivity

    fun setContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        activity = fragment.requireActivity()
        val view = inflater.inflate(R.layout.fragment_unlink_accounts, container, false)

        setLoadingView(view)
        setListView(view)

        return view
    }

    private fun setLoadingView(view: View) {
        progressView = view.findViewById(R.id.view_load_items_progress)
    }

    private fun setListView(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.refreshable_layout)
        swipeRefreshLayout.setOnRefreshListener {
            presenter.onRefresh()
        }
        itemsListView = view.findViewById(R.id.pauses_list)
        layoutEmptyStateViewGroup = view.findViewById(R.id.layout_empty_state)
        val layoutEmptyStateMessage: TextView = view.findViewById(R.id.autofill_list_pauses_empty_state_message)
        layoutEmptyStateMessage.setText(R.string.autofill_unlink_account_items_empty_description)
        layoutErrorStateViewGroup = view.findViewById(R.id.layout_error_state)
        refreshPageButton = view.findViewById(R.id.autofill_list_pauses_error_state_action)
        refreshPageButton.setOnClickListener {
            presenter.onRefresh()
        }
        listAdapter = DashlaneRecyclerAdapter()
        listAdapter.setOnItemClickListener { _, _, _, position ->
            linkedAccountsItemClick(position)
        }
        itemsListView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = listAdapter
        }
        showEmptyState()
    }

    fun onResume() {
        presenter.onResume()
    }

    fun linkedAccountsItemClick(position: Int) {
        presenter.onLinkedAccountsItemClick(position)
    }

    override fun unlinkAccount(account: SummaryObject.Authentifiant, formSourceTitle: String) {
        val autofillUnlinkMessage = fragment.getString(
            R.string.autofill_unlinkaccount_unlink_message,
            formSourceTitle
        )
        toaster.show(autofillUnlinkMessage, Toast.LENGTH_SHORT)
    }

    override fun updateLinkedAccounts(items: List<SummaryObject.Authentifiant>) {
        if (items.isEmpty()) {
            showEmptyState()
        } else {
            showList()
        }
        listAdapter.populateItems(
            items.map {
                linkedAccountViewTypeProviderFactory.create(it)
            }
        )
    }

    override fun showErrorOnLoadLinkedAccounts() {
        itemsListView.visibility = View.GONE
        layoutEmptyStateViewGroup.visibility = View.GONE
        layoutErrorStateViewGroup.visibility = View.VISIBLE
    }

    override fun showErrorOnUnlinkAccount() {
        val message = activity.getString(R.string.autofill_unlink_account_error_message)
        toaster.show(message, Toast.LENGTH_SHORT)
    }

    private fun showList() {
        itemsListView.visibility = View.VISIBLE
        layoutEmptyStateViewGroup.visibility = View.GONE
        layoutErrorStateViewGroup.visibility = View.GONE
    }

    private fun showEmptyState() {
        itemsListView.visibility = View.GONE
        layoutEmptyStateViewGroup.visibility = View.VISIBLE
        layoutErrorStateViewGroup.visibility = View.GONE
    }

    override fun startLoading() {
        progressView.visibility = View.VISIBLE
        swipeRefreshLayout.isRefreshing = true
    }

    override fun stopLoading() {
        progressView.visibility = View.INVISIBLE
        swipeRefreshLayout.isRefreshing = false
    }
}
