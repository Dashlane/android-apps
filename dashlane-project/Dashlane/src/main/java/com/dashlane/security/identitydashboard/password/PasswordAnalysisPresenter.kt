package com.dashlane.security.identitydashboard.password

import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleCoroutineScope
import com.dashlane.R
import com.dashlane.breach.Breach
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.similarpassword.GroupOfPassword
import com.dashlane.ui.activities.fragments.list.wrapper.DefaultVaultItemWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.adapter.ItemListContext.Container
import com.dashlane.url.UrlDomain
import com.dashlane.url.registry.UrlDomainCategory
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.url.toUrlOrNull
import com.dashlane.useractivity.log.usage.UsageLogCode125
import com.dashlane.util.Toaster
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.safelyStartBrowserActivity
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PasswordAnalysisPresenter(
    private val coroutineScope: LifecycleCoroutineScope,
    private val toaster: Toaster,
    private val navigator: Navigator
) :
    BasePresenter<PasswordAnalysisContract.DataProvider,
            PasswordAnalysisContract.ViewProxy>(),
    PasswordAnalysisContract.Presenter,
    PasswordAnalysisItemWrapper.ActionListener {

    lateinit var logger: PasswordAnalysisLogger
    var defaultDestination: String? = null
    var focusBreachIdPending: String? = null
    private var sensitiveAccountOnly = false
    private var securityResult: AuthentifiantSecurityEvaluator.Result? = null

    override fun onViewVisible() {
        logger.onViewVisible()
        requireRefresh(false)
        provider.listenForChanges()
    }

    override fun onViewHidden() {
        view.removeListenerPage()
        provider.unlistenForChanges()
    }

    override fun requireRefresh(forceUpdate: Boolean) {
        coroutineScope.launchWhenResumed {
            if (forceUpdate) {
                logger.nextLogIsUpdate = true
            }
            showAsRefreshing()
            val result = provider.getAuthentifiantsSecurityInfo()
            if (result != null && provider.shouldDisplayProcessDuration()) {
                toaster.show(result.timeMeasurement.toString(), Toast.LENGTH_LONG)
            }

            securityResult = result
            refreshSecurityScore(result?.securityScore ?: -1F)
            refreshList()
        }
    }

    override fun setSensitiveAccountOnly(enable: Boolean) {
        sensitiveAccountOnly = enable
        logger.nextLogIsUpdate = true
        refreshList()
    }

    override fun onListItemClick(item: Any) {
        val authentifiant = (item as? VaultItemWrapper<*>)?.itemObject as? SummaryObject.Authentifiant ?: return
        open(authentifiant)
    }

    override fun saveModified(
        authentifiant: VaultItem<SyncObject.Authentifiant>,
        reason: UsageLogCode125.Action
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            provider.saveModified(authentifiant)
            logger.onAction(reason, authentifiant.syncObject.urlForUsageLog)
            requireRefresh(true)
        }
    }

    override fun open(authentifiant: SummaryObject.Authentifiant) {
        logger.onAction(UsageLogCode125.Action.OPEN_DETAILS, authentifiant.urlForUsageLog)
        navigator.goToCredentialFromPasswordAnalysis(authentifiant.id)
    }

    override fun goToWebsite(authentifiant: SummaryObject.Authentifiant) {
        val activity = this.activity ?: return
        val url = authentifiant.urlForGoToWebsite
            ?.takeIf { it.isNotSemanticallyNull() }
            ?.toUrlOrNull()
            ?: return
        logger.onAction(UsageLogCode125.Action.GO_TO_WEBSITE, authentifiant.urlForUsageLog)
        val browserIntent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .applyAppTheme()
            .build().intent
        browserIntent.data = Uri.parse(url.toString())
        browserIntent.fallbackCustomTab(activity.packageManager)
        activity.safelyStartBrowserActivity(browserIntent)
    }

    override fun onPageSelected(mode: PasswordAnalysisContract.Mode) {
        val securityScore = securityResult?.securityScore ?: -1F
        setCurrentPageView(mode.toPage())
        logger.onPageSelected(mode, sensitiveAccountOnly, securityScore)
    }

    private fun refreshList() {
        val sensitiveDomains = securityResult?.sensitiveDomains

        val modeToSelect = defaultDestination?.let {
            defaultDestination = null 
            tryOrNull { PasswordAnalysisContract.Mode.valueOf(it.uppercase()) }
        } ?: PasswordAnalysisContract.Mode.COMPROMISED.takeIf { focusBreachIdPending != null }
        var indexToHighlight: Int? = null

        val itemsPerMode = PasswordAnalysisContract.Mode.values()
            .associate { mode ->
                val listFilteredAndSorted = getFilteredSortedListByMode(mode)
                val items = ArrayList<Any>()

                val cachedItemWrapper = mutableMapOf<SyncObject.Authentifiant, PasswordAnalysisItemWrapper>()
                listFilteredAndSorted.forEach { groupOfAuthentifiant ->

                    if (mode == modeToSelect) {
                        
                        focusBreachIdPending?.takeIf {
                            mode == PasswordAnalysisContract.Mode.COMPROMISED
                        }?.let { breachIdTarget ->
                            if ((groupOfAuthentifiant.groupBy as? Breach)?.id == breachIdTarget) {
                                indexToHighlight = items.size
                            }
                        }
                    }

                    items.addItems(groupOfAuthentifiant, cachedItemWrapper, mode, sensitiveDomains, this)
                }
                mode to items
            }

        view.setItems(itemsPerMode, modeToSelect, indexToHighlight)
        view.setRefreshMode(false)

        
        focusBreachIdPending = null
    }

    private fun ArrayList<Any>.addItems(
        groupOfAuthentifiant: GroupOfAuthentifiant<*>,
        cachedItemWrapper: MutableMap<SyncObject.Authentifiant, PasswordAnalysisItemWrapper>,
        mode: PasswordAnalysisContract.Mode,
        sensitiveDomains: Map<UrlDomain, UrlDomainCategory?>?,
        listener: PasswordAnalysisItemWrapper.ActionListener
    ) {
        val authentifiants = groupOfAuthentifiant.authentifiants.let { list ->
            if (sensitiveAccountOnly) {
                list.filter {
                    sensitiveDomains?.get(it.navigationUrl?.toUrlDomainOrNull())?.isDataSensitive ?: false
                }
            } else {
                list
            }
        }.takeIf { it.isNotEmpty() }
            ?: return 

        val extra = when (mode) {
            PasswordAnalysisContract.Mode.REUSED ->
                "${groupOfAuthentifiant.countReal}"
            else -> ""
        }

        getHeaderTitle(mode, groupOfAuthentifiant.groupBy, extra)?.let { add(HeaderItem(it)) }

        addAll(
            authentifiants.map {
            cachedItemWrapper.getOrPut(it.item) {
                
                PasswordAnalysisItemWrapper(
                    mode,
                    DefaultVaultItemWrapper(it.item.toSummary(), Container.PASSWORD_HEALTH.asListContext()),
                    listener
                )
            }
        }
        )
    }

    private fun showAsRefreshing() {
        view.setRefreshMode(true)
    }

    private fun refreshSecurityScore(score: Float) {
        view.setSecurityScore(score)
    }

    private fun getHeaderTitle(mode: PasswordAnalysisContract.Mode, header: Any?, extraInfo: String): String? {
        val context = this.context ?: return null
        return when {
            header is PasswordStrength -> {
                header.getShortTitle(context) + " " + extraInfo
            }
            mode == PasswordAnalysisContract.Mode.REUSED && header is GroupOfPassword
            -> {
                String.format(context.getString(R.string.security_dashboard_header_reused), extraInfo)
            }
            mode == PasswordAnalysisContract.Mode.COMPROMISED && header is Breach
            -> {
                val date = header.getDateEventFormated(context)
                String.format(context.getString(R.string.security_dashboard_header_breach), header.title, date)
            }
            else -> null
        }
    }

    private fun getFilteredSortedListByMode(mode: PasswordAnalysisContract.Mode): List<GroupOfAuthentifiant<*>> {
        val result = securityResult ?: return listOf()
        return when (mode) {
            PasswordAnalysisContract.Mode.WEAK ->
                result.authentifiantsByStrength
                    .sortedBy { it.groupBy }
            PasswordAnalysisContract.Mode.REUSED ->
                result.authentifiantsBySimilarity
                    .sortedWith(
                        compareByDescending<GroupOfAuthentifiant<GroupOfPassword>> { it.countReal }
                        .thenBy { it.groupBy.initialPassword }
                    )
            PasswordAnalysisContract.Mode.EXCLUDED ->
                listOf(GroupOfAuthentifiant(null, result.authentifiantsIgnored))
            PasswordAnalysisContract.Mode.COMPROMISED ->
                result.authentifiantsByBreach
                    .sortedWith(compareByDescending { it.groupBy.breachCreationDate })
        }
    }
}

private fun PasswordAnalysisContract.Mode.toPage(): AnyPage = when (this) {
    PasswordAnalysisContract.Mode.COMPROMISED -> AnyPage.TOOLS_PASSWORD_HEALTH_LIST_COMPROMISED
    PasswordAnalysisContract.Mode.EXCLUDED -> AnyPage.TOOLS_PASSWORD_HEALTH_LIST_EXCLUDED
    PasswordAnalysisContract.Mode.REUSED -> AnyPage.TOOLS_PASSWORD_HEALTH_LIST_REUSED
    PasswordAnalysisContract.Mode.WEAK -> AnyPage.TOOLS_PASSWORD_HEALTH_LIST_WEAK
}