package com.dashlane.login

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.getMostRecentDevice
import com.dashlane.authentication.toDevice
import com.dashlane.data.login.Device
import com.dashlane.data.login.SecurityFeature
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.getDevicesLimitValue
import com.dashlane.login.LoginStrategy.Strategy.DeviceLimit
import com.dashlane.login.LoginStrategy.Strategy.Enforce2FA
import com.dashlane.login.LoginStrategy.Strategy.Monobucket
import com.dashlane.login.LoginStrategy.Strategy.NoStrategy
import com.dashlane.login.monobucket.MonobucketUseCase
import com.dashlane.login.pages.enforce2fa.HasEnforced2FaLimitUseCase
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.Session
import com.dashlane.session.authorization
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginStrategy @Inject constructor(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val listDevicesService: ListDevicesService,
    private val hasEnforced2faLimitUseCase: HasEnforced2FaLimitUseCase,
    private val accountStatusRepository: AccountStatusRepository,
    @DefaultCoroutineDispatcher private val defaultCoroutineDispatcher: CoroutineDispatcher,
) {
    sealed class Strategy {
        data object NoStrategy : Strategy()

        data object Unlock : Strategy()

        data object MplessD2D : Strategy()

        data class Monobucket(val device: Device) : Strategy()

        data class DeviceLimit(val devices: List<Device>, val limit: Int) : Strategy()

        data object Enforce2FA : Strategy()
    }

    suspend fun getStrategy(session: Session, securityFeatureSet: Set<SecurityFeature>? = null): Strategy =
        withContext(defaultCoroutineDispatcher) {
            
            runCatching { accountStatusRepository.refreshFor(session) }
            if (hasEnforced2faLimit(session = session, securityFeatureSet = securityFeatureSet)) {
                return@withContext Enforce2FA
            }
            val listDevicesData =
                runCatching { listDevicesService.execute(session.authorization).data }.getOrNull() ?: return@withContext NoStrategy
            if (userFeaturesChecker.has(Capability.DEVICESLIMIT)) {
                val limit = userFeaturesChecker.getDevicesLimitValue()
                val (count, devices) = listDevicesData.getDevicesAndCount()
                if (limit > 1 && count > limit) return@withContext DeviceLimit(devices = devices, limit = limit)
            }
            val monobucketOwner = MonobucketUseCase.getMonobucketOwner(userFeaturesChecker, listDevicesData)
            if (monobucketOwner != null) {
                return@withContext Monobucket(monobucketOwner)
            }
            return@withContext NoStrategy
        }

    private suspend fun hasEnforced2faLimit(
        session: Session,
        securityFeatureSet: Set<SecurityFeature>?,
    ): Boolean {
        val hasTotpSetupFallback = securityFeatureSet?.contains(SecurityFeature.TOTP)
        return hasEnforced2faLimitUseCase(
            session = session,
            hasTotpSetupFallback = hasTotpSetupFallback,
        )
    }

    private fun ListDevicesService.Data.getDevicesAndCount(): Pair<Int, List<Device>> {
        val devices = mutableListOf<Device>()
        val pairingGroups = pairingGroups
        val unpairedDevices = this.devices.map { it.id }.toMutableList()
        
        pairingGroups.forEach { group ->
            unpairedDevices.removeIf { it in group.deviceIds }
            
            val device = group.getMostRecentDevice(this.devices)
            if (device != null) devices.add(device)
        }
        this.devices.filter { it.id in unpairedDevices }.forEach {
            
            devices.add(it.toDevice())
        }
        
        return unpairedDevices.size + pairingGroups.size to devices
    }
}