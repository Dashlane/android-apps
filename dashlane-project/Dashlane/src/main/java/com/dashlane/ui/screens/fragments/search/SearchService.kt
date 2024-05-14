package com.dashlane.ui.screens.fragments.search

import android.view.View
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.markedAsSearchedAsync
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.fab.FabViewProxy
import com.dashlane.ui.fab.VaultFabViewProxy
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

interface SearchService {

    fun getDefaultSearchRequest(): SearchRequest
    fun markedItemAsSearched(itemId: String, syncObjectType: SyncObjectType)
    fun refreshLastActionTimeStamp()

    fun navigateToItem(item: SummaryObject)
    fun navigateToQuickAction(item: SummaryObject, itemListContext: ItemListContext)
    fun navigateToSettings(settingsId: String?, origin: String?)
    fun popBackStack()

    fun getVaultItemLogger(): VaultItemLogger

    fun provideFabViewProxy(layout: View): FabViewProxy
}

class SearchServiceImpl @Inject constructor(
    private val frequentSearch: FrequentSearch,
    private val sessionManager: SessionManager,
    private val lockRepository: LockRepository,
    private val vaultItemLogger: VaultItemLogger,
    private val navigator: Navigator,
    private val teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
    private val passwordLimiter: PasswordLimiter,
) : SearchService {

    override fun getDefaultSearchRequest() = SearchRequest.DefaultRequest.FromRecent

    override fun markedItemAsSearched(itemId: String, syncObjectType: SyncObjectType) {
        frequentSearch.markedAsSearchedAsync(itemId = itemId, syncObjectType = syncObjectType)
    }

    override fun refreshLastActionTimeStamp() {
        sessionManager.session?.let {
            lockRepository.getLockManager(it).setLastActionTimestampToNow()
        }
    }

    override fun navigateToItem(item: SummaryObject) {
        navigator.goToItem(item.id, item.syncObjectType.xmlObjectName)
    }

    override fun navigateToQuickAction(item: SummaryObject, itemListContext: ItemListContext) {
        navigator.goToQuickActions(item.id, itemListContext)
    }

    override fun navigateToSettings(settingsId: String?, origin: String?) {
        navigator.goToSettings(settingsId = settingsId)
    }

    override fun popBackStack() {
        navigator.popBackStack()
    }

    override fun getVaultItemLogger(): VaultItemLogger = vaultItemLogger

    override fun provideFabViewProxy(layout: View): FabViewProxy = VaultFabViewProxy(
        rootView = layout,
        teamspaceRestrictionNotificator = teamspaceRestrictionNotificator,
        navigator = navigator,
        passwordLimiter = passwordLimiter,
    )
}
