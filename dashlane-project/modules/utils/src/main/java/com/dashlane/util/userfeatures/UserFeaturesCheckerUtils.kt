package com.dashlane.util.userfeatures

import com.dashlane.util.userfeatures.UserFeaturesChecker.Capability
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.IN_TEAM
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.NOT_AVAILABLE
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.NOT_PREMIUM
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.NO_PAYMENT
import com.dashlane.util.userfeatures.UserFeaturesChecker.CapabilityReason.UNPAID_FAMILY_MEMBER

fun UserFeaturesChecker.getDevicesLimitValue() =
    getFeatureInfo(Capability.DEVICES_LIMIT).optInt("limit")

fun UserFeaturesChecker.getVpnAccessDeniedReason(): UserFeaturesChecker.CapabilityReason? =
    when (getFeatureInfo(Capability.VPN_ACCESS).optString("reason")) {
        IN_TEAM.value -> IN_TEAM
        NOT_PREMIUM.value -> NOT_PREMIUM
        NO_PAYMENT.value -> NO_PAYMENT
        NOT_AVAILABLE.value -> NOT_AVAILABLE
        UNPAID_FAMILY_MEMBER.value -> UNPAID_FAMILY_MEMBER
        else -> null
    }

fun UserFeaturesChecker.canUpgradeToGetVpn(): Boolean {
    val reason = getVpnAccessDeniedReason()
    return reason != IN_TEAM && reason != NOT_AVAILABLE && reason != UNPAID_FAMILY_MEMBER
}

fun UserFeaturesChecker.canShowVpn(): Boolean {
    val reason = getVpnAccessDeniedReason()
    return has(Capability.VPN_ACCESS) || (reason != NOT_AVAILABLE && reason != UNPAID_FAMILY_MEMBER)
}

fun UserFeaturesChecker.getFamilyBundleLimitValue() =
    getFeatureInfo(Capability.FAMILY_BUNDLE).optInt("limit")