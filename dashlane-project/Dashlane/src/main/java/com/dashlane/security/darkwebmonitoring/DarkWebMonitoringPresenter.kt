package com.dashlane.security.darkwebmonitoring

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.dashlane.R
import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.darkweb.DarkWebEmailStatus.Companion.STATUS_PENDING
import com.dashlane.darkweb.ui.setup.DarkWebSetupMailActivity
import com.dashlane.lock.LockManager
import com.dashlane.navigation.Navigator
import com.dashlane.security.darkwebmonitoring.item.DarkWebBreachItem
import com.dashlane.security.darkwebmonitoring.item.DarkWebEmailItem
import com.dashlane.security.darkwebmonitoring.item.DarkWebEmptyItem
import com.dashlane.security.darkwebmonitoring.item.DarkWebHeaderItem
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.empty.EmptyScreenConfiguration.Builder
import com.dashlane.utils.coroutines.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ObsoleteCoroutinesApi::class)
class DarkWebMonitoringPresenter @Inject constructor(
    @FragmentLifecycleCoroutineScope
    private val fragmentLifecycleCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    dataProvider: DarkWebMonitoringContract.DataProvider,
    private val lockManager: LockManager,
    private val navigator: Navigator,
    private val viewModel: DarkWebMonitoringAlertViewModel
) :
    BasePresenter<DarkWebMonitoringContract.DataProvider, DarkWebMonitoringContract.ViewProxy>(),
    DarkWebMonitoringContract.Presenter,
    DarkWebEmailItem.DeleteListener {

    private val emailAppIntent = Intent.makeMainSelectorActivity(
        Intent.ACTION_MAIN,
        Intent.CATEGORY_APP_EMAIL
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    override var selectedItems: MutableList<DarkWebBreachItem> = mutableListOf()

    init {
        setProvider(dataProvider)
    }

    private fun refreshView(state: DarkWebStatus) {
        val context = context ?: return
        val breaches = state.breaches
        val emailStatuses = state.emailStatus
        if (emailStatuses.isNullOrEmpty()) {
            view.showDarkwebInactiveScene()
            return
        }
        val pendingBreaches =
            breaches.filter { it.localBreach.status != SyncObject.SecurityBreach.Status.SOLVED }
        val pendingItems = if (pendingBreaches.isEmpty() && emailStatuses.any { it.status == STATUS_PENDING }
        ) {
            
            listOf(
                DarkWebEmptyItem(
                    Builder()
                        .setImage(getDrawable(context, R.drawable.ic_empty_breaches_email_pending))
                        .setLine2(context.getString(R.string.empty_dwm_breaches_email_pending))
                        .apply {
                            if (emailAppIntent.resolveActivity(context.packageManager) != null) {
                                setButton(context.getString(R.string.darkweb_setup_result_button_open_email_app)) {
                                    context.startActivity(emailAppIntent)
                                }
                            }
                        }
                        .build()
                )
            )
        } else {
            createBreachItems(
                pendingBreaches,
                DarkWebEmptyItem(
                    Builder()
                        .setImage(getDrawable(context, R.drawable.ic_empty_breaches))
                        .setLine2(context.getString(R.string.empty_dwm_breaches))
                        .build()
                )
            )
        }
        val resolvedItems = createBreachItems(
            breaches.filter { it.localBreach.status == SyncObject.SecurityBreach.Status.SOLVED },
            DarkWebEmptyItem(
                Builder()
                    .setImage(getDrawable(context, R.drawable.ic_empty_breaches_resolved))
                    .setLine2(context.getString(R.string.empty_dwm_breaches_resolved))
                    .setButton(context.getString(R.string.empty_dwm_breaches_button)) {
                        view.goToPendingTab()
                    }
                    .build()
            )
        )
        val deleteListenerReference = this
        val emails = emailStatuses.map {
            DarkWebEmailItem(it).apply {
                deleteListener = deleteListenerReference
            }
        }
        view.setItems(pendingItems, resolvedItems, emails)
    }

    private fun createBreachItems(
        breaches: List<BreachWrapper>?,
        emptyItem: DarkWebEmptyItem
    ): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        if (!breaches.isNullOrEmpty()) {
            breaches
                .groupBy { it.publicBreach.eventDateParsed }
                .forEach { (breachDate, list) ->
                    items.add(
                        DarkWebHeaderItem(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                .format(breachDate.atZone(ZoneId.systemDefault()))
                        )
                    )
                    items.addAll(list.map { DarkWebBreachItem(it) })
                }
        } else {
            items.add(emptyItem)
        }
        return items
    }

    private var pendingDeferredValuesJob: Job? = null

    override fun onViewVisible() {
        provider.listenForChanges()
        fragmentLifecycleCoroutineScope.launch {
            viewModel.breachesState.collect {
                when (it) {
                    null -> view.showLoadingScreen()
                    else -> refreshView(it)
                }
            }
        }
    }

    override fun onViewHidden() {
        pendingDeferredValuesJob?.cancel()
        provider.unlistenForChanges()

        
        selectedItems.clear()
        view.updateActionBar(updateTitle = false)
    }

    override fun requireRefresh() {
        fragmentLifecycleCoroutineScope.launch { refreshList() }
    }

    override fun onClick(item: DashlaneRecyclerAdapter.ViewTypeProvider) {
        when (item) {
            is DarkWebBreachItem -> {
                navigator.goToBreachAlertDetail(item.breach)
            }
        }
    }

    override fun onDeleteClicked(item: DarkWebEmailStatus) {
        fragmentLifecycleCoroutineScope.launch {
            provider.unlistenDarkWeb(item.email)
            refreshList()
        }
    }

    override fun onInactiveDarkwebCtaClick() {
        navigateToSetupMailDarkWeb()
    }

    override fun onAddDarkWebEmailClick() {
        navigateToSetupMailDarkWeb()
    }

    private suspend fun refreshList() {
        lockManager.waitUnlock()
        viewModel.refresh()
    }

    private fun navigateToSetupMailDarkWeb() {
        val context = this.context ?: return
        val intent = Intent(context, DarkWebSetupMailActivity::class.java)
        context.startActivity(intent)
    }

    override fun onCreateOptionsMenu(inflater: MenuInflater, menu: Menu) {
        if (selectedItems.isNotEmpty()) {
            menu.clear()
            inflater.inflate(R.menu.delete_menu, menu)
        }
        view.updateActionBar(updateTitle = true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (selectedItems.isNotEmpty()) {
                    selectedItems.clear()
                    view.updateActionBar(updateTitle = true)
                    activity?.invalidateOptionsMenu()
                    true
                } else {
                    false
                }
            }
            R.id.menu_delete -> {
                deleteSelectedItems()
                true
            }
            else -> false
        }
    }

    private fun deleteSelectedItems() {
        fragmentLifecycleCoroutineScope.launch(mainCoroutineDispatcher) {
            val itemsToDelete = selectedItems.size
            provider.deleteBreaches(selectedItems.map { it.breach })
            selectedItems.clear()
            activity?.invalidateOptionsMenu()
            view.showDeleteCompleted(itemsToDelete)
            refreshList()
        }
    }
}
