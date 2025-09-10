package com.dashlane.login.monobucket

import com.dashlane.authentication.getMostRecentDevice
import com.dashlane.authentication.toDevice
import com.dashlane.data.login.Device
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager

object MonobucketUseCase {
    fun getMonobucketOwner(userFeaturesChecker: UserFeaturesChecker, listDevicesData: ListDevicesService.Data): Device? {
        val isPremiumUser = userFeaturesChecker.has(Capability.SYNC)
        if (isPremiumUser) return null

        return listDevicesData.devices.firstOrNull { it.isBucketOwner == true }?.toDevice()
            ?: run {
                val bucketOwnerPairingGroup = listDevicesData.pairingGroups.firstOrNull { it.isBucketOwner == true } ?: return null
                bucketOwnerPairingGroup.getMostRecentDevice(listDevicesData.devices)
            }
    }

    fun confirmUnregisterDevice(
        logRepository: LogRepository,
        preferencesManager: PreferencesManager,
        sessionManager: SessionManager,
    ) {
        logUserActionStep2(logRepository, chosenAction = com.dashlane.hermes.generated.definitions.CallToAction.UNLINK)
        preferencesManager[sessionManager.session?.username].run {
            ukiRequiresMonobucketConfirmation = false
            isOnLoginPaywall = false
        }
    }

    suspend fun logout(
        logRepository: LogRepository,
        preferencesManager: PreferencesManager,
        sessionManager: SessionManager,
    ) {
        logUserActionStep1(logRepository, chosenAction = null)
        preferencesManager[sessionManager.session?.username].isOnLoginPaywall = false
        sessionManager.session?.let {
            sessionManager.destroySession(it, false)
        }
    }

    fun logUserActionStep1(logRepository: LogRepository, chosenAction: com.dashlane.hermes.generated.definitions.CallToAction?) =
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(
                    com.dashlane.hermes.generated.definitions.CallToAction.ALL_OFFERS,
                    com.dashlane.hermes.generated.definitions.CallToAction.UNLINK,
                ),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction,
            ),
        )

    fun logUserActionStep2(logRepository: LogRepository, chosenAction: com.dashlane.hermes.generated.definitions.CallToAction?) =
        logRepository.queueEvent(
            CallToAction(
                callToActionList = listOf(com.dashlane.hermes.generated.definitions.CallToAction.UNLINK),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction,
            ),
        )
}