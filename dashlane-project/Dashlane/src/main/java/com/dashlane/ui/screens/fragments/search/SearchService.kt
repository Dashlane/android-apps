package com.dashlane.ui.screens.fragments.search

import android.view.View
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.markedAsSearchedAsync
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.fab.FabViewProxy
import com.dashlane.ui.fab.VaultFabViewProxy
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject



interface SearchService {

    fun getDefaultSearchRequest(): SearchRequest
    fun markedItemAsSearched(itemId: String, syncObjectType: SyncObjectType)
    fun refreshLastActionTimeStamp()

    fun navigateToItem(item: SummaryObject)
    fun navigateToSettings(settingsId: String?, origin: String?)
    fun popBackStack()

    fun getVaultItemLogger(): VaultItemLogger

    fun provideFabViewProxy(layout: View): FabViewProxy
}

class SearchServiceImpl @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val sessionManager: SessionManager,
    private val lockRepository: LockRepository,
    private val vaultItemLogger: VaultItemLogger,
    private val navigator: Navigator,
    private val teamspaceAccessor: OptionalProvider<TeamspaceAccessor>
) : SearchService {

    private val frequentSearch: FrequentSearch
        get() = mainDataAccessor.getFrequentSearch()

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
        navigator.goToItem(item.id, item.syncObjectType.desktopId)
    }

    override fun navigateToSettings(settingsId: String?, origin: String?) {
        navigator.goToSettings(settingsId = settingsId, origin = origin)
    }

    override fun popBackStack() {
        navigator.popBackStack()
    }

    override fun getVaultItemLogger(): VaultItemLogger = vaultItemLogger

    override fun provideFabViewProxy(layout: View): FabViewProxy = VaultFabViewProxy(
        layout,
        teamspaceAccessor,
        navigator
    )
}
