package com.dashlane.ui.activities.fragments.vault

import android.content.Context
import android.os.Bundle
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.AnnouncementTags
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Mood
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierDeletedEvent
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.feature.home.data.Filter
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.canUseSecrets
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.frozenaccount.tracking.FrozenStateLogger
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.limitations.PasswordLimitationLogger
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.limitations.PasswordLimiter.Companion.PASSWORD_LIMIT_LOOMING
import com.dashlane.lock.LockManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.session.SessionManager
import com.dashlane.sync.getAgnosticMessageFeedback
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.setCurrentPageView
import com.dashlane.utils.coroutines.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
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
    private val frozenStateLogger: FrozenStateLogger,
    private val sessionManager: SessionManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val frozenStateManager: FrozenStateManager,
    private val userFeaturesChecker: UserFeaturesChecker,
) : BasePresenter<Vault.DataProvider, Vault.View>(),
    Vault.Presenter {

    override val filter = MutableStateFlow(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)

    private val requireContext: Context
        get() = context!!

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
        
        fragmentLifecycleCoroutineScope.launch {
            accountStatusRepository.accountStatusState
                .map { accountStatuses -> accountStatuses[sessionManager.session] }
                .filterNotNull()
                .take(1)
                .collect {
                    view.showTabs(canUseSecrets = userFeaturesChecker.canUseSecrets())
                    selectTab(arguments = arguments, savedInstanceState = savedInstanceState)
                }
        }
    }

    override fun onStartFragment() {
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            
            mayShowAnnouncement()
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

    private fun selectTab(savedInstanceState: Bundle?, arguments: Bundle?) {
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

    private fun onAccountFrozenAnnouncementClicked() {
        frozenStateLogger.logVaultBannerClicked()
        navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
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
            frozenStateManager.isAccountFrozen -> {
                view.showAnnouncement(
                    iconToken = IconTokens.feedbackFailOutlined,
                    title = requireContext.getString(R.string.vault_announcement_frozen_account_title),
                    description = requireContext.getString(
                        R.string.vault_announcement_frozen_account_description,
                        passwordLimiter.passwordLimitCount.toString()
                    ),
                    mood = Mood.Danger,
                    onClick = this::onAccountFrozenAnnouncementClicked
                )
            }
            passwordLimiter.isPasswordLimitReached() -> {
                view.showAnnouncement(
                    iconToken = IconTokens.premiumOutlined,
                    description = requireContext.getString(R.string.vault_announcement_password_limit_title),
                    mood = Mood.Warning,
                    onClick = this::onPasswordLimitAnnouncementClicked
                )
            }
            passwordLimiter.passwordRemainingBeforeLimit() in 0..PASSWORD_LIMIT_LOOMING -> {
                view.showAnnouncement(
                    iconToken = IconTokens.premiumOutlined,
                    description = requireContext.getString(
                        R.string.vault_announcement_password_limit_remaining_title,
                        passwordLimiter.passwordRemainingBeforeLimit()
                    ),
                    mood = Mood.Brand,
                    onClick = this::onPasswordLimitAnnouncementClicked
                )
            }
            inAppLoginManager.isDisabledForApp() -> {
                view.showAnnouncement(
                    iconToken = IconTokens.featureAutofillOutlined,
                    description = requireContext.getString(R.string.vault_announcement_autofill_title),
                    mood = Mood.Neutral,
                    onClick = this::onAutofillAnnouncementClicked
                )
            }
            else -> {
                view.clearAnnouncements()
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
    Filter.FILTER_SECRET -> AnyPage.ITEM_SECRET_LIST
}