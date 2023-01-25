package com.dashlane.util.userfeatures

import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.IN_TEAM
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.NOT_AVAILABLE
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.NOT_PREMIUM
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.NO_PAYMENT

fun UserFeaturesChecker.getDevicesLimitValue() =
    getFeatureInfo(Capability.DEVICES_LIMIT).optInt("limit")

fun UserFeaturesChecker.getVpnAccessDeniedReason() =
    when (getFeatureInfo(Capability.VPN_ACCESS).optString("reason")) {
        IN_TEAM.value -> IN_TEAM
        NOT_PREMIUM.value -> NOT_PREMIUM
        NO_PAYMENT.value -> NO_PAYMENT
        NOT_AVAILABLE.value -> NOT_AVAILABLE
        else -> null
    }

fun UserFeaturesChecker.canUpgradeToGetVpn(): Boolean {
    val reason = getVpnAccessDeniedReason()
    return reason != IN_TEAM && reason != NOT_AVAILABLE
}



fun UserFeaturesChecker.canShowVpn() =
    has(Capability.VPN_ACCESS) || getVpnAccessDeniedReason() != NOT_AVAILABLE

fun UserFeaturesChecker.getFamilyBundleLimitValue() =
    getFeatureInfo(Capability.FAMILY_BUNDLE).optInt("limit")