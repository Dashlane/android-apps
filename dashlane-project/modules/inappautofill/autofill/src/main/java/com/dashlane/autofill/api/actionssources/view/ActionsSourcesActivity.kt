package com.dashlane.autofill.api.actionssources.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.actionssources.model.ActionsSourcesError
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.ToasterImpl
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject



@AndroidEntryPoint
class ActionsSourcesActivity : DashlaneActivity() {

    @Inject
    lateinit var navigationService: AutofillNavigationService

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    private lateinit var logType: String

    private lateinit var progressView: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var actionSourcesHeader: TextView
    private lateinit var itemsListView: RecyclerView
    private lateinit var listPausesAdapter: DashlaneRecyclerAdapter<AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper>
    private lateinit var layoutEmptyStateViewGroup: ViewGroup
    private lateinit var layoutErrorStateViewGroup: ViewGroup
    private lateinit var refreshPageButton: Button

    private val viewModel: ActionsSourcesViewModel by viewModels()
    private val toaster: ToasterImpl = ToasterImpl(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_AUTOFILL)
        logType = intent.getStringExtra(EXTRA_ORIGIN) ?: DEFAULT_LOG_ORIGIN

        setContentView(R.layout.activity_autofill_actions_sources)

        setToolBar()
        setLoadingView()
        setListView()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.actionsSourcesStateFlow.collect { render(it) } }
                launch {
                    viewModel.selectedItemFlow.collect {
                        navigationService.navigateToRevertActions(this@ActionsSourcesActivity, it, "settings")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.viewResumed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun render(state: ActionsSourcesState) {
        showLoading(state.data.isLoading)
        when (state) {
            is ActionsSourcesState.Error -> showError(state.error)
            is ActionsSourcesState.Success -> {
                if (state.data.itemList.isEmpty()) {
                    showEmptyState()
                } else {
                    showList()
                    listPausesAdapter.populateItems(state.data.itemList)
                }
            }
            else -> Unit
        }
    }

    private fun setToolBar() {
        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES_IN_CONTEXT)) {
                getString(R.string.autofill_actioned_sources_paused_title)
            } else {
                getString(R.string.autofill_actioned_sources_title)
            }
        }
    }

    private fun setLoadingView() {
        progressView = findViewById(R.id.view_load_items_progress)
    }

    private fun setListView() {
        actionSourcesHeader = findViewById(R.id.tv_all)
        swipeRefreshLayout = findViewById(R.id.refreshable_layout)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefresh()
        }
        itemsListView = findViewById(R.id.pauses_list)
        layoutEmptyStateViewGroup = findViewById(R.id.layout_empty_state)
        val layoutEmptyStateMessage: TextView = findViewById(R.id.autofill_list_pauses_empty_state_message)
        layoutEmptyStateMessage.setText(R.string.autofill_actioned_sources_items_empty_description)
        layoutErrorStateViewGroup = findViewById(R.id.layout_error_state)
        refreshPageButton = findViewById(R.id.autofill_list_pauses_error_state_action)
        refreshPageButton.setOnClickListener {
            viewModel.onRefresh()
        }
        listPausesAdapter = DashlaneRecyclerAdapter()
        listPausesAdapter.setOnItemClickListener { _, _, _, position ->
            viewModel.onFormSourcesItemClick(position)
        }
        itemsListView.run {
            layoutManager = LinearLayoutManager(this@ActionsSourcesActivity)
            adapter = listPausesAdapter
        }
        showEmptyState()
    }

    private fun showList() {
        actionSourcesHeader.visibility = View.VISIBLE
        itemsListView.visibility = View.VISIBLE
        layoutEmptyStateViewGroup.visibility = View.GONE
        layoutErrorStateViewGroup.visibility = View.GONE
    }

    private fun showEmptyState() {
        actionSourcesHeader.visibility = View.GONE
        itemsListView.visibility = View.GONE
        layoutEmptyStateViewGroup.visibility = View.VISIBLE
        layoutErrorStateViewGroup.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressView.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = true
        } else {
            progressView.visibility = View.INVISIBLE
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showError(error: ActionsSourcesError) {
        when (error) {
            ActionsSourcesError.AllSelectItem -> {
                val message = getString(R.string.autofill_actioned_sources_forget_error_message)
                toaster.show(message, Toast.LENGTH_SHORT)
            }
            ActionsSourcesError.LoadAllFormSources -> {
                actionSourcesHeader.visibility = View.GONE
                itemsListView.visibility = View.GONE
                layoutEmptyStateViewGroup.visibility = View.GONE
                layoutErrorStateViewGroup.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val EXTRA_ORIGIN = "origin"
        private const val DEFAULT_LOG_ORIGIN = "settings"
    }
}
