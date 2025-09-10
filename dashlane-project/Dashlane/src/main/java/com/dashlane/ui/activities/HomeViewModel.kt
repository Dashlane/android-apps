package com.dashlane.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.abtesting.OfflineExperimentReporter
import com.dashlane.abtesting.RemoteAbTestManager
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockWatcher
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.PreferencesManager
import com.dashlane.premium.StoreOffersCache
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.sync.DataSync
import com.dashlane.ui.premium.inappbilling.BillingVerification
import com.dashlane.update.AppUpdateInstaller
import com.google.android.play.core.appupdate.AppUpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.take
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val featureFlipManager: FeatureFlipManager,
    private val remoteAbTestManager: RemoteAbTestManager,
    private val offlineExperimentReporter: OfflineExperimentReporter,
    private val billingVerification: BillingVerification,
    private val deviceUpdateManager: DeviceUpdateManager,
    private val storeOffersCache: StoreOffersCache,
    private val dataSync: DataSync,
    private val appUpdateInstaller: AppUpdateInstaller,
    private val lockWatcher: LockWatcher,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<HomeState.View, HomeState.SideEffect>(HomeState.View.Initial)
    val stateFlow: ViewStateFlow<HomeState.View, HomeState.SideEffect> = _stateFlow

    fun viewStarted() {
        sessionManager.session?.authorization?.let { authorization ->
            featureFlipManager.launchRefreshIfNeeded(authorization)
            remoteAbTestManager.launchRefreshIfNeeded(authorization)
            
            offlineExperimentReporter.launchReportIfNeeded(authorization)
        }

        billingVerification.verifyAndConsumePurchaseIfNeeded()
        deviceUpdateManager.updateIfNeeded()
    }

    fun viewResumed() {
        storeOffersCache.prefetchProductsForCurrentUser(viewModelScope)
        dataSync.maySync()

        lockWatcher.lockEventSharedFlow
            .filterIsInstance<LockEvent.Unlock>()
            .take(1)
            .combine(appUpdateInstaller.sharedFlow) { _, appUpdateInfo ->
                showUpdateNeeded(appUpdateInfo)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun showUpdateNeeded(appUpdateInfo: AppUpdateInfo) {
        val userPreferencesManager = preferencesManager[sessionManager.session?.username]
        val nextReminderDate = userPreferencesManager.lastShownAvailableUpdateDate.plus(Duration.ofDays(30))
        
        if (Instant.now().isBefore(nextReminderDate)) return
        userPreferencesManager.lastShownAvailableUpdateDate = Instant.now()
        _stateFlow.send(HomeState.SideEffect.InstallUpdate(appUpdateInfo))
    }
}