package com.dashlane.security.darkwebmonitoring.detail

import android.graphics.Typeface
import android.os.Build
import android.text.style.StyleSpan
import androidx.lifecycle.LifecycleCoroutineScope
import com.dashlane.R
import com.dashlane.breach.Breach
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.security.darkwebmonitoring.DarkWebMonitoringAlertViewModel
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.security.identitydashboard.breach.getDataInvolvedFormatted
import com.dashlane.util.getFormattedSpannable
import com.dashlane.util.setCurrentPageView
import com.dashlane.utils.coroutines.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.model.urlDomain
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BreachAlertDetailPresenter @Inject constructor(
    dataProvider: BreachAlertDetail.DataProvider,
    initialBreach: BreachWrapper,
    @FragmentLifecycleCoroutineScope
    private val fragmentLifecycleCoroutineScope: CoroutineScope,
    private val viewModel: DarkWebMonitoringAlertViewModel,
    private val navigator: Navigator,
    private val logger: BreachAlertDetail.Logger,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher
) : BasePresenter<BreachAlertDetail.DataProvider, BreachAlertDetail.ViewProxy>(), BreachAlertDetail.Presenter {

    private var breachWrapper = initialBreach
        set(value) {
            field = value
            onStart()
        }
    private var hasFollowedAdvice = false

    init {
        setProvider(dataProvider)
    }

    override fun onStart() {
        activity?.let {
            val page = if (breachWrapper.publicBreach.isDarkWebBreach()) {
                AnyPage.TOOLS_DARK_WEB_MONITORING_ALERT
            } else {
                AnyPage.NOTIFICATION_SECURITY_DETAILS
            }
            setCurrentPageView(page)
        }
        setupTitle()
        setupDomain()
        setupEmail()
        setupDate()
        setupPassword()
        setupOtherData()
        setupAdvices()

        if (breachWrapper.localBreach.status != SyncObject.SecurityBreach.Status.SOLVED && !hasFollowedAdvice) {
            fragmentLifecycleCoroutineScope.launch {
                provider.markAsViewed(breachWrapper)
            }
        }
    }

    override fun onResume() {
        if (hasFollowedAdvice) {
            viewModel.refresh()
            hasFollowedAdvice = false
        }
    }

    private fun setupTitle() {
        view.updateTitle(breachWrapper.publicBreach.isDarkWebBreach(), breachWrapper.localBreach.solved)
    }

    private fun setupDomain() {
        val domain = breachWrapper.publicBreach.domains?.firstOrNull()
        view.setDomain(domain)

        val domains = breachWrapper.publicBreach.domains
        if (domains != null && domains.size > 1) {
            view.showWebsite(domains.joinToString())
        } else {
            view.hideWebsite()
        }
    }

    private fun setupEmail() {
        view.setEmails(breachWrapper.publicBreach.impactedEmails)
    }

    private fun setupDate() {
        breachWrapper.publicBreach.getDateEventFormated(view.context)?.let {
            view.setDate(it)
        }
    }

    private fun setupPassword() {
        view.setPassword(breachWrapper.localBreach.leakedPasswordsSet.joinToString())
    }

    private fun setupOtherData() {
        if (breachWrapper.publicBreach.leakedData != null) {
            
            val exclude = if (breachWrapper.localBreach.leakedPasswordsSet.isNotEmpty()) {
                setOf(Breach.DATA_PASSWORD)
            } else {
                emptySet()
            }

            view.setDataInvolved(breachWrapper.publicBreach.getDataInvolvedFormatted(view.context, exclude))
        }
    }

    private fun setupAdvices() {
        
        if (breachWrapper.localBreach.solved) {
            setupResolvedAdvice()
            return
        }

        val advicesInfoBox = mutableSetOf<BreachAlertAdvice>()
        setupPasswordAdvice()?.also {
            advicesInfoBox.add(it)
        }
        setupCreditCardAdvice()?.also {
            advicesInfoBox.add(it)
        }
        view.showAdvicesInfoBox(advicesInfoBox)
    }

    private fun setupResolvedAdvice() = if (breachWrapper.publicBreach.hasPasswordLeaked()) {
        view.showAdvicesInfoBox(
            setOf(
                BreachAlertAdvice(
                    view.resources.getString(R.string.dwm_alert_detail_advice_password_changed),
                    true
                )
            )
        )
    } else {
        view.showAdvicesInfoBox(emptySet())
    }

    private fun setupPasswordAdvice(): BreachAlertAdvice? {
        if (breachWrapper.publicBreach.hasPasswordLeaked()) {
            val passwordAdvice: String
            val resolved: Boolean
            var passwordAdviceButton: String? = null
            var passwordAdviceButtonAction: (() -> Unit)? = null
            when {
                breachWrapper.linkedAuthentifiant.size > 1 -> {
                    passwordAdvice = view.resources.getString(
                        R.string.dwm_alert_detail_advice_password_multiple,
                        breachWrapper.linkedAuthentifiant.size
                    )
                    passwordAdviceButton = view.resources.getString(R.string.dwm_alert_detail_view_accounts_cta)
                    passwordAdviceButtonAction = {
                        hasFollowedAdvice = true
                        viewAffectedAccounts()
                    }
                    resolved = false
                }
                breachWrapper.linkedAuthentifiant.size == 1 -> {
                    passwordAdvice = view.resources.getString(R.string.dwm_alert_detail_advice_password)
                    passwordAdviceButton = view.resources.getString(R.string.dwm_alert_detail_change_password_cta)
                    passwordAdviceButtonAction = {
                        fragmentLifecycleCoroutineScope.launch(mainDispatcher) {
                            val itemId = breachWrapper.linkedAuthentifiant.first()
                            val credential = provider.getCredential(itemId)
                            val userName = credential?.login ?: credential?.email
                            hasFollowedAdvice = true
                            changeSinglePassword(itemId, userName, credential?.urlDomain)
                        }
                    }
                    resolved = false
                }
                else -> {
                    passwordAdvice = view.resources.getString(R.string.dwm_alert_detail_advice_password_changed)
                    resolved = true
                }
            }
            return BreachAlertAdvice(passwordAdvice, resolved, passwordAdviceButton, passwordAdviceButtonAction)
        }
        return null
    }

    private fun setupCreditCardAdvice(): BreachAlertAdvice? {
        if (breachWrapper.publicBreach.hasCreditCardLeaked()) {
            val domain = breachWrapper.publicBreach.domains?.firstOrNull()
            val creditCardAdvice = if (domain != null) {
                view.resources.getFormattedSpannable(
                    R.string.dwm_alert_detail_advice_creditcard,
                    domain,
                    listOf(StyleSpan(Typeface.BOLD))
                )
            } else {
                view.resources.getString(R.string.dwm_alert_detail_advice_creditcard_nodomain)
            }
            return BreachAlertAdvice(
                creditCardAdvice.toString(),
                false,
                view.resources.getString(R.string.dwm_alert_detail_mark_done_cta)
            ) {
                fragmentLifecycleCoroutineScope.launch(mainDispatcher) {
                    provider.markAsResolved(breachWrapper)
                    viewModel.refresh()
                }
            }
        }
        return null
    }

    private fun changeSinglePassword(itemId: String, email: String?, domain: String?) =
        if (domain != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            navigator.goToGuidedPasswordChange(
                itemId,
                domain,
                email
            )
        } else {
            
            navigator.goToItem(itemId, SyncObjectType.AUTHENTIFIANT.xmlObjectName)
        }

    private fun viewAffectedAccounts() {
        navigator.goToPasswordAnalysisFromBreach(
            breachWrapper.publicBreach.id
        )
    }

    override fun deleteBreach(activityLifecycleScope: LifecycleCoroutineScope) {
        logger.logDelete(breachWrapper)
        fragmentLifecycleCoroutineScope.launch(mainDispatcher) {
            provider.deleteBreach(breachWrapper)
            view.showUndoDeletion(activity!!) {
                activityLifecycleScope.launch(mainDispatcher) {
                    provider.restoreBreach(breachWrapper)
                    viewModel.refresh()
                }
            }
            navigator.popBackStack()
        }
    }
}