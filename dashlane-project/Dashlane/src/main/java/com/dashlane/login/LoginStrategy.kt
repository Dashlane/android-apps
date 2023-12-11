package com.dashlane.login

import com.dashlane.authentication.SecurityFeature
import com.dashlane.core.premium.PremiumStatusManager
import com.dashlane.login.LoginStrategy.Strategy.DEVICE_LIMIT
import com.dashlane.login.LoginStrategy.Strategy.ENFORCE_2FA
import com.dashlane.login.LoginStrategy.Strategy.MONOBUCKET
import com.dashlane.login.LoginStrategy.Strategy.NO_STRATEGY
import com.dashlane.login.monobucket.MonobucketHelper
import com.dashlane.login.pages.enforce2fa.HasEnforced2FaLimitUseCaseImpl
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.session.Session
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.getDevicesLimitValue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext

@Singleton
class LoginStrategy @Inject constructor(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val listDevicesService: ListDevicesService,
    private val premiumStatusManager: PremiumStatusManager,
    private val hasEnforced2faLimitUseCase: HasEnforced2FaLimitUseCaseImpl
) {
    lateinit var monobucketHelper: MonobucketHelper
    lateinit var devices: MutableList<Device>

    enum class Strategy {
        NO_STRATEGY,

        MONOBUCKET,

        DEVICE_LIMIT,

        ENFORCE_2FA,
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    suspend fun getStrategy(session: Session, securityFeatureSet: Set<SecurityFeature>? = null): Strategy =
        withContext(Dispatchers.Default) {
            
            val premiumStatusDeferred = refreshPremiumStatusAsync(session)
            val listDevicesResponseDeferred = getListDevicesAsync(session)
            joinAll(premiumStatusDeferred, listDevicesResponseDeferred)
            if (hasEnforced2faLimit(session = session, securityFeatureSet = securityFeatureSet)) {
                return@withContext ENFORCE_2FA
            }
            val listDevicesResponse = listDevicesResponseDeferred.getCompleted() ?: return@withContext NO_STRATEGY
            val listDevicesData = listDevicesResponse.data
            if (userFeaturesChecker.has(Capability.DEVICES_LIMIT)) {
                val limit = userFeaturesChecker.getDevicesLimitValue()
                if (limit > 1 && getDevicesCount(listDevicesData) > limit) return@withContext DEVICE_LIMIT
            }
            monobucketHelper = MonobucketHelper(userFeaturesChecker, listDevicesData)
            if (monobucketHelper.getMonobucketOwner() != null) {
                return@withContext MONOBUCKET
            }
            return@withContext NO_STRATEGY
        }

    private fun CoroutineScope.getListDevicesAsync(session: Session) =
        async {
            try {
                listDevicesService.execute(session.authorization)
            } catch (ignored: Throwable) {
                null
            }
        }

    private fun CoroutineScope.refreshPremiumStatusAsync(session: Session) =
        async {
            try {
                premiumStatusManager.refreshPremiumStatus(session)
            } catch (e: Exception) {
                
            }
        }

    private suspend fun hasEnforced2faLimit(
        session: Session,
        securityFeatureSet: Set<SecurityFeature>?
    ): Boolean {
        val hasTotpSetupFallback = securityFeatureSet?.contains(SecurityFeature.TOTP)
        return hasEnforced2faLimitUseCase(
            session = session,
            hasTotpSetupFallback = hasTotpSetupFallback
        )
    }

    private fun getDevicesCount(listDevicesData: ListDevicesService.Data): Int {
        devices = mutableListOf()
        val pairingGroups = listDevicesData.pairingGroups
        val unpairedDevices = listDevicesData.devices.map { it.id }.toMutableList()
        
        pairingGroups.forEach { group ->
            unpairedDevices.removeIf { it in group.deviceIds }
            
            val device = group.getMostRecentDevice(listDevicesData.devices)
            if (device != null) devices.add(device)
        }
        listDevicesData.devices.filter { it.id in unpairedDevices }.forEach {
            
            devices.add(it.toDevice())
        }
        
        return unpairedDevices.size + pairingGroups.size
    }
}
