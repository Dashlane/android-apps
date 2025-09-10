package com.dashlane.authentication

import com.dashlane.data.login.Device
import com.dashlane.data.login.SecurityFeature
import com.dashlane.data.login.SsoInfo
import com.dashlane.data.login.TermsOfService
import com.dashlane.server.api.endpoints.account.Consent
import com.dashlane.server.api.endpoints.authentication.AuthSsoInfo
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.time.toInstant
import com.dashlane.user.UserSecuritySettings
import com.dashlane.vault.model.DeviceType

internal fun AuthSsoInfo.toAuthenticationSsoInfo(login: String) = SsoInfo(
    login = login,
    serviceProviderUrl = serviceProviderUrl,
    isNitroProvider = isNitroProvider ?: false,
    migration = migration?.let {
        when (it) {
            AuthSsoInfo.Migration.SSO_MEMBER_TO_MP_USER,
            AuthSsoInfo.Migration.SSO_MEMBER_TO_ADMIN,
            -> SsoInfo.Migration.TO_MASTER_PASSWORD_USER
            AuthSsoInfo.Migration.MP_USER_TO_SSO_MEMBER -> SsoInfo.Migration.TO_SSO_MEMBER
        }
    },
)

internal fun TermsOfService.toConsentsList() = listOfNotNull(
    conditions?.toConsent(Consent.ConsentType.PRIVACYPOLICYANDTOS),
    offers?.toConsent(Consent.ConsentType.EMAILSOFFERSANDTIPS),
)

private fun Boolean.toConsent(type: Consent.ConsentType) = Consent(
    consentType = type,
    status = this,
)

fun ListDevicesService.Data.Device.toDevice(groupId: String? = null) = Device(
    id = id,
    name = name.orEmpty(),
    iconResId = DeviceType.forValue(platform).iconResId,
    lastActivityDate = lastActivityDate.toInstant().toEpochMilli(),
    pairingGroupId = groupId,
)

fun ListDevicesService.Data.PairingGroup.getMostRecentDevice(allDevices: List<ListDevicesService.Data.Device>): Device? {
    val deviceIds = deviceIds.toSet()
    val devices = allDevices.filter { it.id in deviceIds }
    if (devices.isEmpty()) return null
    val desktopDevices = devices.filter { DeviceType.forValue(it.platform).isDesktop }
    return if (desktopDevices.isEmpty()) {
        devices.maxByOrNull { it.updateDate }?.toDevice(id)
    } else {
        desktopDevices.maxByOrNull { it.updateDate }?.toDevice(id)
    }
}

fun UserSecuritySettings?.toSecurityFeatures(): Set<SecurityFeature> {
    this ?: return emptySet()
    return sequence {
        if (isToken) yield(SecurityFeature.EMAIL_TOKEN)
        if (isTotp) yield(SecurityFeature.TOTP)
        if (isDuoEnabled) yield(SecurityFeature.DUO)
        if (isU2fEnabled) yield(SecurityFeature.U2F)
        if (isSso) yield(SecurityFeature.SSO)
    }.toSet()
}