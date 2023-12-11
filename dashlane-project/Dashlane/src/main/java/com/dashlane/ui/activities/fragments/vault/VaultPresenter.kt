package com.dashlane.ui.activities.fragments.vault

import android.os.Bundle
import android.view.View
import com.dashlane.R
import com.dashlane.core.sync.getAgnosticMessageFeedback
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.setCurrentPageView
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class VaultPresenter @Inject constructor(
    dataProvider: VaultDataProvider,
    @FragmentLifecycleCoroutineScope
    private val fragmentLifecycleCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val lockManager: LockManager,
    private val appEvents: AppEvents,
    private val navigator: Navigator,
    private val vaultViewModel: VaultViewModel,
    private val inAppLoginManager: InAppLoginManager,
) : BasePresenter<Vault.DataProvider, Vault.View>(),
Vault.Presenter,
    EfficientAdapter.OnItemClickListener<DashlaneRecyclerAdapter.ViewTypeProvider> {

    override val filter = MutableStateFlow(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)

    init {
        setProvider(dataProvider)
    }

    override fun onCreate(arguments: Bundle?, savedInstanceState: Bundle?) {
        val filter = if (savedInstanceState == null && arguments != null) {
            VaultFragmentArgs.fromBundle(arguments).filter?.let { getFilterFromPath(it) }
        } else if (savedInstanceState != null) {
            savedInstanceState.getString(EXTRA_CURRENT_FILTER)?.let { Filter.valueOf(it) }
        } else {
            null
        }
        if (filter == null) {
            
            setCurrentPageView(AnyPage.ITEM_ALL_LIST)
        } else {
            this.filter.value = filter
            view.setSelectedFilterTab(filter)
        }
    }

    override fun onStartFragment() {
        provider.subscribeTeamspaceManager()
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            onSyncFinished(it)
        }
    }

    override fun onStopFragment() {
        provider.unsubscribeTeamspaceManager()
        appEvents.unregister(this, SyncFinishedEvent::class.java)
    }

    override fun onResumeFragment() {
        mayShowAnnouncement()
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_CURRENT_FILTER, filter.value.name)
    }

    override fun onTeamspaceChange(teamspace: Teamspace?) {
        refresh()
    }

    override fun onSearchViewClicked() {
        navigator.goToSearch()
    }

    private fun refresh() {
        if (!lockManager.isLocked) {
            refreshItemList()
            return
        }
        
        fragmentLifecycleCoroutineScope.launch(mainCoroutineDispatcher) {
            lockManager.waitUnlock()
            refreshItemList()
        }
    }

    private fun refreshItemList() {
        vaultViewModel.refresh()
    }

    override fun onItemClick(
        adapter: EfficientAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>,
        view: View,
        item: DashlaneRecyclerAdapter.ViewTypeProvider?,
        position: Int,
    ) {
        if (item is VaultItemViewTypeProvider) {
            navigator.goToItem(item.summaryObject.id, item.summaryObject.syncObjectType.xmlObjectName)
        }
    }

    override fun onFilterSelected(filter: Filter) {
        setCurrentPageView(filter.toPage())
        this.filter.value = filter
    }

    override fun onTabClicked(filter: Filter) = Unit

    override fun onTabReselected(filter: Filter) {
        if (this.filter.value == filter) {
            appEvents.post(ScrollToTopEvent(filter.ordinal))
        }
    }

    override fun onMenuAlertClicked() {
        navigator.goToActionCenter()
    }

    private fun onAutofillAnnouncementClicked() {
        navigator.goToInAppLogin()
    }

    private fun onSyncFinished(syncFinishedEvent: SyncFinishedEvent) {
        if (activity == null) {
            return
        }
        refresh()
        syncFinishedEvent.getAgnosticMessageFeedback()?.let { message ->
            viewOrNull?.showSnackbar(stringRes = message)
        }
    }

    private fun getFilterFromPath(path: String?): Filter? = when (path) {
        NavigationHelper.Destination.MainPath.PASSWORDS -> Filter.FILTER_PASSWORD
        NavigationHelper.Destination.MainPath.NOTES -> Filter.FILTER_SECURE_NOTE
        NavigationHelper.Destination.MainPath.PAYMENTS -> Filter.FILTER_PAYMENT
        NavigationHelper.Destination.MainPath.PERSONAL_INFO -> Filter.FILTER_PERSONAL_INFO
        NavigationHelper.Destination.MainPath.ID_DOCUMENT -> Filter.FILTER_ID
        else -> null
    }

    private fun mayShowAnnouncement() {
        if (inAppLoginManager.isDisabledForApp()) {
            view.showAnnouncement(
                R.layout.include_layout_vault_autofill_announcement,
                onClick = this::onAutofillAnnouncementClicked
            )
        } else {
            view.showAnnouncement(null)
        }
    }

    companion object {
        const val EXTRA_CURRENT_FILTER = "extra_current_filter"
    }
}

private fun Filter.toPage(): AnyPage = when (this) {
    Filter.ALL_VISIBLE_VAULT_ITEM_TYPES -> AnyPage.ITEM_ALL_LIST
    Filter.FILTER_PASSWORD -> AnyPage.ITEM_CREDENTIAL_LIST
    Filter.FILTER_SECURE_NOTE -> AnyPage.ITEM_SECURE_NOTE_LIST
    Filter.FILTER_PAYMENT -> AnyPage.ITEM_PAYMENT_LIST
    Filter.FILTER_PERSONAL_INFO -> AnyPage.ITEM_PERSONAL_INFO_LIST
    Filter.FILTER_ID -> AnyPage.ITEM_ID_LIST
}