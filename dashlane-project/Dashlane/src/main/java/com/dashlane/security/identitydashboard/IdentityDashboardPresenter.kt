package com.dashlane.security.identitydashboard

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dashlane.R
import com.dashlane.help.HelpCenterLink
import com.dashlane.login.lock.LockManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.security.identitydashboard.item.IdentityDashboardItem
import com.dashlane.security.identitydashboard.item.IdentityDashboardPasswordHealthItem
import com.dashlane.security.identitydashboard.item.IdentityDashboardSeparatorItem
import com.dashlane.security.identitydashboard.item.identityprotection.IdentityDashboardProtectionPackageActiveItem
import com.dashlane.security.identitydashboard.item.identityprotection.IdentityDashboardProtectionPackageLoggerImpl
import com.dashlane.util.Toaster
import com.dashlane.util.launchUrl
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IdentityDashboardPresenter @Inject constructor(
    private val lockManager: LockManager,
    private val toaster: Toaster,
    private val protectionPackageLogger: IdentityDashboardProtectionPackageLoggerImpl,
    private val navigator: Navigator
) :
    BasePresenter<IdentityDashboardContract.DataProvider, IdentityDashboardContract.ViewProxy>(),
    IdentityDashboardContract.Presenter,
    IdentityDashboardPasswordHealthItem.PasswordHeathClickListener,
    IdentityDashboardProtectionPackageActiveItem.ActiveProtectionPackageListener {

    private val dashboardPasswordHealthItem = IdentityDashboardPasswordHealthItem(null, this)

    @OptIn(DelicateCoroutinesApi::class)
    var coroutineScope: CoroutineScope = GlobalScope
        set(value) {
            field = value
            dashboardPasswordHealthItem.coroutineScope = value
        }

    override fun onViewVisible() {
        provider.listenForChanges()
        coroutineScope.launch(Dispatchers.Main) { refreshList() }
    }

    override fun onViewHidden() {
        provider.unlistenForChanges()
    }

    override fun requireRefresh() {
        coroutineScope.launch(Dispatchers.Main) { refreshList() }
    }

    override fun onClick(item: IdentityDashboardItem) {
        when (item) {
            is IdentityDashboardPasswordHealthItem ->
                navigateToPasswordHealth()
        }
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

    override fun logOnActivePackageShow() {
        protectionPackageLogger.logOnActivePackageShow()
    }

    override fun onActiveCreditViewClick() {
        val context = this.context ?: return
        protectionPackageLogger.logOnActiveSeeCreditView()
        coroutineScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.Default) { provider.getCreditMonitoringLink() }
            if (result != null && result != CreditMonitoringManager.ERROR_UNKNOWN) {
                context.launchUrl(result)
            } else {
                toaster.show(resources!!.getString(R.string.general_error), Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onActiveProtectionLearnMoreClick() {
        val context = this.context ?: return
        protectionPackageLogger.logOnActiveProtectionLearnMore()
        openCustomTab(HelpCenterLink.ARTICLE_IDENTITY_PROTECTION.uri, context)
    }

    override fun onActiveRestorationLearnMoreClick() {
        val context = this.context ?: return
        protectionPackageLogger.logOnActiveRestorationLearnMore()
        openCustomTab(HelpCenterLink.ARTICLE_IDENTITY_RESTORATION.uri, context)
    }

    private fun openCustomTab(helpLink: Uri, context: Context) {
        context.launchUrl(helpLink)
    }

    private suspend fun refreshList() {
        lockManager.waitUnlock()

        val items = mutableListOf<IdentityDashboardItem>()
        
        items.add(dashboardPasswordHealthItem.apply {
            futureSecurityScoreResult = provider.getAuthentifiantsSecurityInfoAsync()
        })

        
        if (provider.hasProtectionPackage()) {
            items.add(IdentityDashboardSeparatorItem())
            items.add(IdentityDashboardProtectionPackageActiveItem(this, protectionPackageLogger))
        }

        view.setItems(items)
    }

    private fun navigateToPasswordHealth(tab: String? = null) {
        navigator.goToPasswordAnalysisFromIdentityDashboard("identity_dashboard", tab)
    }
}
