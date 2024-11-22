package com.dashlane.security.identitydashboard

import android.content.Context
import android.net.Uri
import com.dashlane.help.HelpCenterLink
import com.dashlane.lock.LockManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.security.identitydashboard.item.IdentityDashboardItem
import com.dashlane.security.identitydashboard.item.IdentityDashboardPasswordHealthItem
import com.dashlane.security.identitydashboard.item.IdentityDashboardSeparatorItem
import com.dashlane.security.identitydashboard.item.identityprotection.IdentityDashboardProtectionPackageActiveItem
import com.dashlane.util.launchUrl
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class IdentityDashboardPresenter @Inject constructor(
    private val lockManager: LockManager,
    private val navigator: Navigator,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher
) :
    BasePresenter<IdentityDashboardContract.DataProvider, IdentityDashboardContract.ViewProxy>(),
    IdentityDashboardContract.Presenter,
    IdentityDashboardPasswordHealthItem.PasswordHeathClickListener,
    IdentityDashboardProtectionPackageActiveItem.ActiveProtectionPackageListener {

    private val dashboardPasswordHealthItem = IdentityDashboardPasswordHealthItem(null, this)

    var coroutineScope: CoroutineScope = applicationCoroutineScope
        set(value) {
            field = value
            dashboardPasswordHealthItem.coroutineScope = value
        }

    @Suppress("kotlin:S6311")
    override fun onViewVisible() {
        provider.listenForChanges()
        coroutineScope.launch(mainCoroutineDispatcher) { refreshList(forceRefresh = false) }
    }

    override fun onViewHidden() {
        provider.unlistenForChanges()
    }

    @Suppress("kotlin:S6311")
    override fun requireRefresh(forceRefresh: Boolean) {
        coroutineScope.launch(mainCoroutineDispatcher) { refreshList(forceRefresh = forceRefresh) }
    }

    override fun onClickExplore() {
        navigateToPasswordHealth()
    }

    override fun onClickCompromised() {
        navigateToPasswordHealth(NavigationHelper.Destination.SecondaryPath.PasswordHealth.COMPROMISED)
    }

    override fun onClickReused() {
        navigateToPasswordHealth(NavigationHelper.Destination.SecondaryPath.PasswordHealth.REUSED)
    }

    override fun onClickWeak() {
        navigateToPasswordHealth(NavigationHelper.Destination.SecondaryPath.PasswordHealth.WEAK)
    }

    override fun onActiveProtectionLearnMoreClick() {
        val context = this.context ?: return
        openCustomTab(HelpCenterLink.ARTICLE_IDENTITY_PROTECTION.androidUri, context)
    }

    override fun onActiveRestorationLearnMoreClick() {
        val context = this.context ?: return
        openCustomTab(HelpCenterLink.ARTICLE_IDENTITY_RESTORATION.androidUri, context)
    }

    private fun openCustomTab(helpLink: Uri, context: Context) {
        context.launchUrl(helpLink)
    }

    private suspend fun refreshList(forceRefresh: Boolean) {
        lockManager.waitUnlock()

        val items = mutableListOf<IdentityDashboardItem>()
        
        items.add(
            dashboardPasswordHealthItem.apply {
                futureSecurityScoreResult = provider.getAuthentifiantsSecurityInfoAsync(forceRefresh)
            }
        )

        
        if (provider.hasProtectionPackage()) {
            items.add(IdentityDashboardSeparatorItem())
            items.add(IdentityDashboardProtectionPackageActiveItem(this))
        }

        view.setItems(items)
    }

    private fun navigateToPasswordHealth(tab: String? = null) {
        navigator.goToPasswordAnalysisFromIdentityDashboard(tab)
    }
}
