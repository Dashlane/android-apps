package com.dashlane.premium.current.other

import androidx.annotation.VisibleForTesting
import com.dashlane.premium.R
import com.dashlane.premium.current.CurrentPlanContract
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.model.TextResource.Arg
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.getDevicesLimitValue
import com.dashlane.util.userfeatures.getFamilyBundleLimitValue

internal class CurrentBenefitsBuilder(
    private val userFeaturesChecker: UserFeaturesChecker
) {
    internal fun build() = buildAllBenefits().filterNotNull()

    @VisibleForTesting
    internal fun buildAllBenefits(): List<CurrentPlan.Benefit?> = listOf(
        getStoringPasswordLimit().withAction(null),
        getDevicesSyncLimit()?.withAction(null),
        getSecureNotesCreation()?.withAction(null),
        getSecureFilesStorage()?.withAction(null),
        getSharing()?.withAction(null),
        getDarkWebMonitoring()?.withAction { openDarkWebMonitoringInfo() },
        getBundleType()?.withAction(null),
        getWifiProtection()?.withAction(null),
        getPremiumPlus()?.withAction(null)
    )

    private fun TextResource.withAction(action: (CurrentPlanContract.Presenter.() -> Unit)?) =
        CurrentPlan.Benefit(textResource = this, action = action)

    private fun getStoringPasswordLimit() = TextResource.StringText(R.string.current_benefit_passwords_unlimited)

    private fun getDevicesSyncLimit() =
        if (userFeaturesChecker.has(Capability.DEVICES_LIMIT)) {
            val limit = userFeaturesChecker.getDevicesLimitValue()
            TextResource.PluralsText(
                pluralsRes = R.plurals.current_benefit_devices_sync_limited,
                quantity = limit,
                arg = Arg.IntArg(limit)
            ).takeIf { limit > 0 }
        } else {
            TextResource.StringText(R.string.current_benefit_devices_sync_unlimited)
        }

    private fun getSecureNotesCreation() =
        TextResource.StringText(R.string.current_benefit_secure_notes)
            .takeIf { userFeaturesChecker.has(Capability.ADD_SECURE_NOTES) }

    private fun getDarkWebMonitoring() =
        TextResource.StringText(R.string.current_benefit_dark_web_monitoring)
            .takeIf { userFeaturesChecker.has(Capability.DATA_LEAK) }

    private fun getBundleType(): TextResource.StringText? {
        if (userFeaturesChecker.has(Capability.FAMILY_BUNDLE)) {
            val limit = userFeaturesChecker.getFamilyBundleLimitValue().takeIf { it > 1 } ?: return null
            return TextResource.StringText(R.string.current_benefit_family_bundle, Arg.StringArg("$limit"))
        } else {
            return null
        }
    }

    private fun getWifiProtection() =
        TextResource.StringText(R.string.current_benefit_vpn)
            .takeIf { userFeaturesChecker.has(Capability.VPN_ACCESS) }

    private fun getPremiumPlus() =
        TextResource.StringText(R.string.current_benefit_premium_plus)
            .takeIf {
                userFeaturesChecker.has(Capability.CREDIT_MONITORING) &&
                    userFeaturesChecker.has(Capability.IDENTITY_RESTORATION) &&
                    userFeaturesChecker.has(Capability.IDENTITY_THEFT_PROTECTION)
            }

    private fun getSecureFilesStorage() =
        TextResource.StringText(R.string.current_benefit_secure_file_storage)
            .takeIf {
                userFeaturesChecker.has(Capability.SECURE_FILES_UPLOAD)
            }

    private fun getSharing() =
        TextResource.StringText(R.string.current_benefit_sharing)
            .takeIf {
                !userFeaturesChecker.has(Capability.SHARING_LIMIT)
            }
}
