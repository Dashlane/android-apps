package com.dashlane.premium.current.other

import androidx.annotation.VisibleForTesting
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.getDevicesLimitValue
import com.dashlane.featureflipping.getFamilyBundleLimitValue
import com.dashlane.featureflipping.getPasswordsLimitValue
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.ui.R
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.model.TextResource.Arg

internal class CurrentBenefitsBuilder(
    private val userFeaturesChecker: UserFeaturesChecker,
) {
    internal fun build(isFamily: Boolean) = buildAllBenefits(isFamily).filterNotNull()

    @VisibleForTesting
    internal fun buildAllBenefits(isFamily: Boolean): List<CurrentPlan.Benefit?> = listOf(
        getStoringPasswordLimit().withMoreInfoType(null),
        getDevicesSyncLimit()?.withMoreInfoType(null),
        getSecureNotesCreation()?.withMoreInfoType(null),
        getSecureFilesStorage()?.withMoreInfoType(null),
        getSharing()?.withMoreInfoType(null),
        getDarkWebMonitoring()?.withMoreInfoType(CurrentPlan.Benefit.MoreInfoType.DARK_WEB_MONITORING),
        getPhishingAlert()?.withMoreInfoType(null),
        getBundleType()?.withMoreInfoType(null),
        getWifiProtection(isFamily)?.withMoreInfoType(null),
        getPremiumPlus()?.withMoreInfoType(null),
    )

    private fun TextResource.withMoreInfoType(moreInfoType: CurrentPlan.Benefit.MoreInfoType?) =
        CurrentPlan.Benefit(textResource = this, moreInfoType = moreInfoType)

    private fun getStoringPasswordLimit(): TextResource {
        val limit = userFeaturesChecker.getPasswordsLimitValue()
        return if (userFeaturesChecker.has(Capability.PASSWORDSLIMIT) && limit > 0) {
            TextResource.PluralsText(
                pluralsRes = R.plurals.current_benefit_passwords_limit,
                quantity = limit,
                arg = Arg.IntArg(limit),
            )
        } else {
            TextResource.StringText(R.string.current_benefit_passwords_unlimited)
        }
    }

    private fun getDevicesSyncLimit() =
        if (userFeaturesChecker.has(Capability.DEVICESLIMIT)) {
            val limit = userFeaturesChecker.getDevicesLimitValue()
            TextResource.PluralsText(
                pluralsRes = R.plurals.current_benefit_devices_sync_limited,
                quantity = limit,
                arg = Arg.IntArg(limit),
            ).takeIf { limit > 0 }
        } else {
            TextResource.StringText(R.string.current_benefit_devices_sync_unlimited)
        }

    private fun getSecureNotesCreation() =
        TextResource.StringText(R.string.current_benefit_secure_notes)
            .takeIf { userFeaturesChecker.has(Capability.SECURENOTES) }

    private fun getDarkWebMonitoring() =
        TextResource.StringText(R.string.current_benefit_dark_web_monitoring)
            .takeIf { userFeaturesChecker.has(Capability.DATALEAK) }

    private fun getPhishingAlert() =
        TextResource.StringText(R.string.current_benefit_phishing_alert)
            .takeIf { userFeaturesChecker.has(Capability.AUTOFILLWITHPHISHINGPREVENTION) }

    private fun getBundleType(): TextResource.StringText? {
        return if (userFeaturesChecker.has(Capability.MULTIPLEACCOUNTS)) {
            val limit = userFeaturesChecker.getFamilyBundleLimitValue().takeIf { it > 1 } ?: return null
            TextResource.StringText(R.string.current_benefit_family_bundle, Arg.StringArg("$limit"))
        } else {
            null
        }
    }

    private fun getWifiProtection(isFamily: Boolean) = if (isFamily) {
        TextResource.StringText(R.string.current_benefit_vpn_family)
    } else {
        TextResource.StringText(R.string.current_benefit_vpn)
    }.takeIf { userFeaturesChecker.has(Capability.SECUREWIFI) }

    private fun getPremiumPlus() =
        TextResource.StringText(R.string.current_benefit_premium_plus)
            .takeIf {
                userFeaturesChecker.has(Capability.CREDITMONITORING) &&
                    userFeaturesChecker.has(Capability.IDENTITYRESTORATION) &&
                    userFeaturesChecker.has(Capability.IDENTITYTHEFTPROTECTION)
            }

    private fun getSecureFilesStorage() =
        TextResource.StringText(R.string.current_benefit_secure_file_storage)
            .takeIf {
                userFeaturesChecker.has(Capability.SECUREFILES)
            }

    private fun getSharing() =
        TextResource.StringText(R.string.current_benefit_sharing)
            .takeIf {
                !userFeaturesChecker.has(Capability.SHARINGLIMIT)
            }
}