package com.dashlane.core.premium

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.events.AppEvents
import com.dashlane.events.PremiumStatusChangedEvent
import com.dashlane.network.webservices.CheckPremiumStatusService
import com.dashlane.network.webservices.GetDevicesNumberService
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.teamspaces.manager.TeamspaceUpdater
import com.dashlane.useractivity.log.usage.UsageLogCode53
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.copyWithPremiumStatus
import com.dashlane.util.Constants
import com.dashlane.util.logW
import dagger.Lazy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PremiumStatusManager @Inject constructor(
    private val preferenceManager: UserPreferencesManager,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val getDevicesNumberService: GetDevicesNumberService,
    private val accountStatusRepository: Lazy<AccountStatusRepository>,
    private val sessionManager: SessionManager,
    private val teamspaceUpdater: TeamspaceUpdater,
    private val userSecureDataStorageManager: UserSecureStorageManager,
    private val appEvents: AppEvents,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    suspend fun refreshPremiumStatus(session: Session): Boolean {
        val username = session.userId
        val uki = session.uki
        val retrofit = SingletonProvider.getComponent().webservicesRetrofit
        val service = retrofit.create(CheckPremiumStatusService::class.java)
        return coroutineScope {
            launch { updateNumberOfDevices(username, uki) }
            runCatching {
                service.execute(
                    username,
                    uki,
                    Constants.getOSLang(),
                    Constants.IN_APP_BILLING.PLATFORM_PLAY_STORE_SUBSCRIPTION
                )
            }
                .mapCatching { response -> PremiumStatus(response, true) }
                .onSuccess { premiumStatus ->
                    savePremiumStatus(premiumStatus)
                    bySessionUsageLogRepository[session]
                        ?.enqueue(
                            UsageLogCode53(country = deviceInfoRepository.deviceCountry)
                                .copyWithPremiumStatus(premiumStatus)
                        )
                }
                .isSuccess
        }
    }

    

    private fun savePremiumStatus(status: PremiumStatus) {
        val session = sessionManager.session ?: return
        val accountStatusRepository = accountStatusRepository.get()
        val lastSaved = accountStatusRepository.getPremiumStatus(session)
        userSecureDataStorageManager.storePremiumServerStatus(session, status.serverValue)
        var prefManager = preferenceManager
        if (status.isRefreshed) {
            prefManager = prefManager.preferencesFor(session.username)
            resetPremiumNotificationIfChanged(prefManager, status, lastSaved)
        }
        accountStatusRepository.reloadStatus(session)
        teamspaceUpdater.processAndSaveTeamspaces(status.teamspaces)

        appEvents.post(PremiumStatusChangedEvent(lastSaved, status))
    }

    private fun resetPremiumNotificationIfChanged(
        preferencesManager: UserPreferencesManager,
        newStatus: PremiumStatus,
        lastSaved: PremiumStatus?
    ) {
        if (lastSaved != null) {
            val deletePref = newStatus.premiumType != lastSaved.premiumType ||
                    newStatus.hasExpiryDateField() != lastSaved.hasExpiryDateField() ||
                    (newStatus.hasExpiryDateField() && lastSaved.hasExpiryDateField() && newStatus.expiryDate != lastSaved.expiryDate)
            if (deletePref) {
                preferencesManager.apply(
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_FIRST_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_SECOND_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_THIRD_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.GRACE_PERIOD_END_NOTIFICATION_DONE
                    )
                )
            }
        }
    }

    private suspend fun updateNumberOfDevices(username: String, uki: String) {
        if (!preferenceManager.exist(ConstantsPrefs.USER_NUMBER_DEVICES)) {
            
            preferenceManager.putInt(ConstantsPrefs.USER_NUMBER_DEVICES, 1)
        }
        val response = try {
            getDevicesNumberService.execute(username, uki)
        } catch (e: IOException) {
            logW(throwable = e) { "Failed to update number of devices" }
            return
        } catch (e: HttpException) {
            logW(throwable = e) { "Failed to update number of devices" }
            return
        }

        if (!response.isSuccess) {
            logW { "Failed to update number of devices. $response" }
            return
        }

        val numberOfDevices = response.numberOfDevices
        preferenceManager.putInt(ConstantsPrefs.USER_NUMBER_DEVICES, numberOfDevices)
    }

    companion object {

        @JvmStatic
        fun create(): PremiumStatusManager {
            val singletonComponent = SingletonProvider.getComponent()
            val dashlaneRetrofit = singletonComponent.webservicesRetrofit
            val getDevicesNumberService =
                dashlaneRetrofit.create(GetDevicesNumberService::class.java)
            val accountStatusRepository = SingletonProvider.getComponent().accountStatusRepository
            val sessionManager = SingletonProvider.getSessionManager()
            val secureDataStorageManager = SingletonProvider.getUserSecureDataStorageManager()
            val teamspaceRepository = SingletonProvider.getComponent().teamspaceRepository
            val appEvents = SingletonProvider.getAppEvents()
            val bySessionUsageLogRepository = SingletonProvider.getComponent().bySessionUsageLogRepository
            val teamspaceForceCategorizationManager = SingletonProvider.getComponent().teamspaceForceCategorizationManager
            return PremiumStatusManager(
                singletonComponent.userPreferencesManager,
                singletonComponent.deviceInfoRepository,
                getDevicesNumberService,
                { accountStatusRepository },
                sessionManager,
                TeamspaceUpdater(sessionManager, teamspaceRepository) { teamspaceForceCategorizationManager },
                secureDataStorageManager,
                appEvents,
                bySessionUsageLogRepository
            )
        }
    }
}
