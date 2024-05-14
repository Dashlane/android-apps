package com.dashlane.ui.activities.fragments.vault

import android.os.Bundle
import android.widget.TextView
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.AnnouncementTags
import com.dashlane.core.sync.getAgnosticMessageFeedback
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierDeletedEvent
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.limitations.PasswordLimitationLogger
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.limitations.PasswordLimiter.Companion.PASSWORD_LIMIT_LOOMING
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.setCurrentPageView
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class VaultPresenter @Inject constructor(
    @FragmentLifecycleCoroutineScope
    private val fragmentLifecycleCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val lockManager: LockManager,
    private val appEvents: AppEvents,
    private val navigator: Navigator,
    private val vaultViewModel: VaultViewModel,
    private val inAppLoginManager: InAppLoginManager,
    private val announcementCenter: AnnouncementCenter,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    private val passwordLimiter: PasswordLimiter,
    private val passwordLimitationLogger: PasswordLimitationLogger,
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository
) : BasePresenter<Vault.DataProvider, Vault.View>(),
    Vault.Presenter {

    override val filter = MutableStateFlow(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)

    init {
        
        fragmentLifecycleCoroutineScope.launch {
            currentTeamSpaceUiFilter.teamSpaceFilterState.collect { filter ->
                onTeamspaceChange(filter.teamSpace)
            }
        }
        
        fragmentLifecycleCoroutineScope.launch {
            accountStatusRepository.accountStatusState.collect { accountStatuses ->
                accountStatuses[sessionManager.session]?.let {
                    refresh()
                }
            }
        }
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
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            onSyncFinished(it)
        }
        appEvents.register(this, DataIdentifierDeletedEvent::class.java, false) {
            
            mayShowAnnouncement()
        }
        announcementCenter.fragment(AnnouncementTags.FRAGMENT_VAULT_LIST)
    }

    override fun onStopFragment() {
        appEvents.unregister(this, SyncFinishedEvent::class.java)
        appEvents.unregister(this, DataIdentifierDeletedEvent::class.java)
        announcementCenter.fragment(null)
    }

    override fun onResumeFragment() {
        mayShowAnnouncement()
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_CURRENT_FILTER, filter.value.name)
    }

    override fun onTeamspaceChange(teamspace: TeamSpace?) {
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

    private fun onPasswordLimitAnnouncementClicked() {
        passwordLimitationLogger.upgradeFromBanner()
        navigator.goToOffers()
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
        when {
            passwordLimiter.isPasswordLimitReached() -> {
                view.showAnnouncement(
                    R.layout.include_vault_password_limit_announcement,
                    onClick = this::onPasswordLimitAnnouncementClicked
                )
            }
            passwordLimiter.passwordRemainingBeforeLimit() in 0..PASSWORD_LIMIT_LOOMING -> {
                val viewCreated = view.showAnnouncement(
                    R.layout.include_vault_password_limit_remaining_announcement,
                    onClick = this::onPasswordLimitAnnouncementClicked
                )
                viewCreated?.findViewById<TextView>(R.id.vault_announcement_text)?.text =
                    viewCreated?.context?.getString(
                        R.string.vault_announcement_password_limit_remaining_title,
                        passwordLimiter.passwordRemainingBeforeLimit()
                    )
            }
            inAppLoginManager.isDisabledForApp() -> {
                view.showAnnouncement(
                    R.layout.include_vault_autofill_announcement,
                    onClick = this::onAutofillAnnouncementClicked
                )
            }
            else -> {
                view.showAnnouncement(null)
            }
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