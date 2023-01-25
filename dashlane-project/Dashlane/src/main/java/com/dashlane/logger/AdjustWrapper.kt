package com.dashlane.logger

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.dashlane.abtesting.LocalAbTest
import com.dashlane.abtesting.LocalAbTestManager
import com.dashlane.abtesting.LocalVariant.ADJUST_TRACKING_ANONYMOUS_DEVICE_ID
import com.dashlane.abtesting.LocalVariant.ADJUST_TRACKING_INSTALLATION_ID
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.Constants
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AdjustWrapper @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val localAbTestManager: LocalAbTestManager
) {
    private var isInit = false

    fun initIfNeeded(context: Context) {
        if (isInit) return 

        val appToken = Constants.MARKETING.ADJUST_API_KEY
        val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
        val config = AdjustConfig(context, appToken, environment)
        config.setPreinstallTrackingEnabled(true)
        Adjust.onCreate(config)

        setTrackingId()

        isInit = true
    }

    

    private fun setTrackingId() {
        val anonymousIdStrategy = createAnonymousIdStrategy()
        val installationIdStrategy = createInstallationIdStrategy()
        when {
            anonymousIdStrategy.isSelected -> anonymousIdStrategy.addSessionCallbackParameter()
            installationIdStrategy.isSelected -> installationIdStrategy.addSessionCallbackParameter()
            else -> {
                when (localAbTestManager.getVariant(LocalAbTest.ADJUST_ID_MIGRATION)) {
                    ADJUST_TRACKING_ANONYMOUS_DEVICE_ID ->
                        selectTrackingIdStrategy(anonymousIdStrategy)
                    ADJUST_TRACKING_INSTALLATION_ID ->
                        selectTrackingIdStrategy(installationIdStrategy)
                }
            }
        }
    }

    private fun selectTrackingIdStrategy(strategy: AdjustTrackingIdStrategy) {
        strategy.addSessionCallbackParameter()
        strategy.sendInstallEvent()
        strategy.isSelected = true
    }

    

    private fun createAnonymousIdStrategy(): AdjustTrackingIdStrategy {
        val anonymousDeviceId =
            deviceInfoRepository.anonymousDeviceId
        return AdjustTrackingIdStrategy(
            id = anonymousDeviceId,
            preferencesKey = "install_receiver_adjust_sent_for_$anonymousDeviceId",
            sessionCallbackParameterKey = "anonymousComputerId",
            installEventPartnerParameterKey = "anonymousDeviceId"
        )
    }

    

    private fun createInstallationIdStrategy(): AdjustTrackingIdStrategy {
        val installationTrackingId =
            globalPreferencesManager.installationTrackingId
        return AdjustTrackingIdStrategy(
            id = installationTrackingId,
            preferencesKey = "install_receiver_adjust_sent_for_installation_id",
            sessionCallbackParameterKey = "installation_id",
            installEventPartnerParameterKey = "installation_id"
        )
    }

    private inner class AdjustTrackingIdStrategy(
        private val id: String,
        private val preferencesKey: String,
        private val sessionCallbackParameterKey: String,
        private val installEventPartnerParameterKey: String
    ) {
        

        var isSelected: Boolean
            get() = globalPreferencesManager.getBoolean(preferencesKey)
            set(value) {
                globalPreferencesManager.putBoolean(preferencesKey, value)
            }

        fun addSessionCallbackParameter() {
            Adjust.addSessionCallbackParameter(sessionCallbackParameterKey, id)
        }

        

        fun sendInstallEvent() {
            val event = AdjustEvent(Constants.MARKETING.ADJUST_EVENT_INSTALL)
            try {
                event.addPartnerParameter(installEventPartnerParameterKey, id)
            } catch (e: Exception) {
                Log.v(e)
            }
            Adjust.trackEvent(event)
        }
    }
}