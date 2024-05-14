package com.dashlane.userfeatures

import com.dashlane.server.api.endpoints.premium.PremiumStatus.Capabilitie.Capability
import com.dashlane.server.api.endpoints.premium.PremiumStatus.Capabilitie.Info.Reason

fun UserFeaturesChecker.getDevicesLimitValue(): Int = (getCapabilityInfo(Capability.DEVICESLIMIT)?.limit ?: 0).toInt()
fun UserFeaturesChecker.getPasswordsLimitValue(): Int =
    (getCapabilityInfo(Capability.PASSWORDSLIMIT)?.limit ?: 0).toInt()

fun UserFeaturesChecker.getVpnAccessDeniedReason(): Reason? =
    getCapabilityInfo(Capability.SECUREWIFI)?.reason

fun UserFeaturesChecker.canUpgradeToGetVpn(): Boolean {
    return when (getVpnAccessDeniedReason()) {
        Reason.NOT_PREMIUM,
        Reason.NO_PAYMENT,
        Reason.DISABLED_FOR_FREE_USER -> true
        else -> false
    }
}

fun UserFeaturesChecker.getSecureFilesMaxFileSize(): Long =
    this.getCapabilityInfo(Capability.SECUREFILES)?.maxFileSize ?: 0

fun UserFeaturesChecker.canShowVpn(): Boolean {
    val reason = getVpnAccessDeniedReason()
    return has(Capability.SECUREWIFI) || (reason != Reason.NO_VPN_CAPABILITY && reason != Reason.IS_UNPAID_FAMILY_MEMBER)
}

fun UserFeaturesChecker.getFamilyBundleLimitValue(): Int =
    (getCapabilityInfo(Capability.MULTIPLEACCOUNTS)?.limit ?: 0).toInt()

@Suppress("DEPRECATION")
fun UserFeaturesChecker.canUseSecureNotes(): Boolean =
    has(Capability.SECURENOTES) && !has(FeatureFlip.DISABLE_SECURE_NOTES)