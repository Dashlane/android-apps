package com.dashlane.login

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.SecurityFeature
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.getDevicesLimitValue
import com.dashlane.login.LoginStrategy.Strategy.DeviceLimit
import com.dashlane.login.LoginStrategy.Strategy.Enforce2FA
import com.dashlane.login.LoginStrategy.Strategy.Monobucket
import com.dashlane.login.LoginStrategy.Strategy.NoStrategy
import com.dashlane.login.monobucket.getMonobucketOwner
import com.dashlane.login.pages.enforce2fa.HasEnforced2FaLimitUseCaseImpl
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.Session
import com.dashlane.session.authorization
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
    private val hasEnforced2faLimitUseCase: HasEnforced2FaLimitUseCaseImpl,
    private val accountStatusRepository: AccountStatusRepository
) {
    lateinit var devices: MutableList<Device>

    sealed class Strategy {

        data object NoStrategy : Strategy()

        data object Unlock : Strategy()

        data object MplessD2D : Strategy()

        data class Monobucket(val device: Device) : Strategy()

        data object DeviceLimit : Strategy()

        data object Enforce2FA : Strategy()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    suspend fun getStrategy(session: Session, securityFeatureSet: Set<SecurityFeature>? = null): Strategy =
        withContext(Dispatchers.Default) {
            
            val premiumStatusDeferred = refreshPremiumStatusAsync(session)
            val listDevicesResponseDeferred = getListDevicesAsync(session)
            joinAll(premiumStatusDeferred, listDevicesResponseDeferred)
            if (hasEnforced2faLimit(session = session, securityFeatureSet = securityFeatureSet)) {
                return@withContext Enforce2FA
            }
            val listDevicesResponse = listDevicesResponseDeferred.getCompleted() ?: return@withContext NoStrategy
            val listDevicesData = listDevicesResponse.data
            if (userFeaturesChecker.has(Capability.DEVICESLIMIT)) {
                val limit = userFeaturesChecker.getDevicesLimitValue()
                if (limit > 1 && getDevicesCount(listDevicesData) > limit) return@withContext DeviceLimit
            }
            val monobucketOwner = getMonobucketOwner(userFeaturesChecker, listDevicesData)
            if (monobucketOwner != null) {
                return@withContext Monobucket(monobucketOwner)
            }
            return@withContext NoStrategy
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
                accountStatusRepository.refreshFor(session)
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
