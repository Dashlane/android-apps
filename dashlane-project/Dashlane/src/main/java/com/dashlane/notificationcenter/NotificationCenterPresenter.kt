package com.dashlane.notificationcenter

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dashlane.biometricrecovery.BiometricRecoveryIntroActivity
import com.dashlane.biometricrecovery.MasterPasswordResetIntroActivity
import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.lock.LockHelper
import com.dashlane.navigation.Navigator
import com.dashlane.notificationcenter.alerts.BreachDataHelper
import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.notificationcenter.view.AlertActionItem
import com.dashlane.notificationcenter.view.HeaderItem
import com.dashlane.notificationcenter.view.NotificationItem
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.security.DashlaneIntent
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationCenterPresenter @Inject constructor(
    fragment: Fragment,
    @FragmentLifecycleCoroutineScope
    private val fragmentLifecycleCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val logger: NotificationCenterLogger,
    private val breachesDataProvider: BreachDataHelper,
    private val biometricAuthModule: BiometricAuthModule,
    private val lockHelper: LockHelper,
    private val sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    private val appEvents: AppEvents,
    private val navigator: Navigator
) : BasePresenter<NotificationCenterDef.DataProvider, NotificationCenterDef.View>(),
    NotificationCenterDef.Presenter {

    var section: ActionItemSection? = null

    private val origin = NotificationCenterLogger.ORIGIN

    init {
        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_START) {
                    provider.listenForChanges()
                } else if (event == Lifecycle.Event.ON_STOP) {
                    provider.unlistenForChanges()
                }
            }
        })
    }

    override fun refresh() {
        fragmentLifecycleCoroutineScope.launch(mainCoroutineDispatcher) {
            val section = this@NotificationCenterPresenter.section
            val list = if (section == null) {
                providerOrNull?.loadAll()
            } else {
                providerOrNull?.load(section)
            } ?: return@launch
            viewOrNull?.items = list
            viewOrNull?.setLoading(false)
            updateBreachAlertCount(list)
            providerOrNull?.refreshNotificationBadge()
            markAllBreachesAsViewed(list)
        }
    }

    override fun click(item: DashlaneRecyclerAdapter.ViewTypeProvider) {
        when (item) {
            is NotificationItem -> {
                logger.logActionItemClick(item.type.trackingKey)
                item.action(this)
            }
            is HeaderItem ->
                startSectionDetails(item.section)
        }
    }

    override fun markAsRead() {
        val list = viewOrNull?.getDisplayedItems() ?: return
        providerOrNull?.markAsRead(list)
    }

    override fun dismiss(item: NotificationItem) {
        logger.logActionItemDismiss(item.type.trackingKey)
        providerOrNull?.dismiss(item)
        fragmentLifecycleCoroutineScope.launch(mainCoroutineDispatcher) {
            saveAndRemoveBreach(item)
        }
    }

    override fun undoDismiss(item: NotificationItem) {
        logger.logActionItemUndoDismiss(item.type.trackingKey)
        providerOrNull?.undoDismiss(item)
        logger.logActionItemShow(item.type.trackingKey)
        fragmentLifecycleCoroutineScope.launch(mainCoroutineDispatcher) { undoSaveAndRemoveBreach(item) }
    }

    override fun startPinCodeSetup() {
        runWhenUnlocked { context?.let { lockHelper.showLockActivityToSetPinCode(it, false) } }
    }

    override fun startBiometricSetup() {
        runWhenUnlocked { context?.run { biometricAuthModule.startOnboarding(this) } }
    }

    override fun startOnboardingInAppLogin() {
        navigator.goToInAppLogin(origin)
    }

    override fun startAddOnePassword() {
        navigator.goToCredentialAddStep1(
            sender = origin,
            expandImportOptions = true,
            successIntent = DashlaneIntent.newInstance(context, HomeActivity::class.java)
        )
    }

    override fun startSectionDetails(section: ActionItemSection) {
        navigator.goToSectionDetailsFromActionCenter(section.name)
    }

    override fun startAlertDetails(breachWrapper: BreachWrapper) {
        navigator.goToBreachAlertDetail(breachWrapper)
    }

    override fun startSharingRedirection() {
        navigator.goToPasswordSharingFromActionCenter()
    }

    override fun startChromeImport() {
        navigator.goToChromeImportIntro(origin)
    }

    override fun startBiometricRecoverySetup(hasBiometricLockType: Boolean) {
        runWhenUnlocked {
            context?.run {
                val intent = if (hasBiometricLockType) {
                    MasterPasswordResetIntroActivity.newIntent(this)
                } else {
                    BiometricRecoveryIntroActivity.newIntent(this)
                }

                startActivity(intent)
            }
        }
    }

    override fun startGoEssentials() {
        navigator.goToOffers(origin = origin, offerType = OfferType.ADVANCED.toString())
    }

    override fun startCurrentPlan() {
        navigator.goToCurrentPlan(origin = origin)
    }

    override fun startUpgrade(offerType: OfferType?) {
        navigator.goToOffers(origin = origin, offerType = offerType?.toString())
    }

    override fun startAuthenticator() {
        navigator.goToAuthenticator()
    }

    private fun runWhenUnlocked(onUnlock: () -> Unit) {
        context?.run {
            sensibleSettingsClickHelper.perform(context = this, onUnlock = onUnlock)
        }
    }

    private suspend fun markAllBreachesAsViewed(list: List<NotificationItem>) {
        list.filterIsInstance(AlertActionItem::class.java)
            .mapIndexed { _, item ->
                item.breachWrapper
            }.run {
                breachesDataProvider.markAllAsViewed(this)
            }
    }

    private suspend fun saveAndRemoveBreach(item: NotificationItem) {
        if (item !is AlertActionItem) return
        breachesDataProvider.saveAndRemove(item.breachWrapper, SyncObject.SecurityBreach.Status.ACKNOWLEDGED)
        appEvents.post(BreachStatusChangedEvent())
    }

    private suspend fun undoSaveAndRemoveBreach(item: NotificationItem) {
        if (item !is AlertActionItem) return
        breachesDataProvider.saveAndRemove(item.breachWrapper, SyncObject.SecurityBreach.Status.VIEWED)
        appEvents.post(BreachStatusChangedEvent())
    }

    override fun groupActionItemsBySection(
        list: List<NotificationItem>,
        displaySectionHeader: Boolean,
        breachAlertCount: Int?
    ):
            List<DashlaneRecyclerAdapter.MultiColumnViewTypeProvider> {
        return if (displaySectionHeader) {
            list.groupBy { it.section }
                .toSortedMap()
                .map { (section, items) ->
                    
                    
                    val count = if (section == ActionItemSection.BREACH_ALERT) breachAlertCount else items.count()
                    val header = HeaderItem(section, count) { headerSection ->
                        startSectionDetails(headerSection)
                    }
                    listOf(header) + items.sortedByDescending { providerOrNull?.getCreationDate(it) }.take(
                        SECTION_CAPPING_THRESHOLD
                    )
                }.flatten()
        } else {
            list.sortedByDescending { providerOrNull?.getCreationDate(it) }
        }
    }

    private suspend fun updateBreachAlertCount(list: List<NotificationItem>) {
        val hasBreachAlert = list.any { it.section == ActionItemSection.BREACH_ALERT }
        if (hasBreachAlert) {
            val breachAlertCount = withContext(Dispatchers.Default) {
                providerOrNull?.getBreachAlertCount()
            }
            viewOrNull?.updateBreachAlertHeader(breachAlertCount)
        }
    }

    companion object {
        const val SECTION_CAPPING_THRESHOLD = 2
    }
}