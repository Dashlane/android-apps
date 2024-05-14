package com.dashlane.premium.offer.details

import com.dashlane.premium.R
import com.dashlane.server.api.endpoints.payments.StoreOffer
import com.dashlane.ui.model.TextResource.Arg.IntArg
import com.dashlane.ui.model.TextResource.Arg.StringArg
import com.dashlane.ui.model.TextResource.Arg.StringResArg
import com.dashlane.ui.model.TextResource.PluralsText
import com.dashlane.ui.model.TextResource.StringText
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.userfeatures.canShowVpn
import java.text.DecimalFormat
import kotlin.math.pow

internal class BenefitsBuilder(
    private val capabilities: StoreOffer.Capabilities,
    private val userFeaturesChecker: UserFeaturesChecker
) {
    internal fun build(isFamily: Boolean) = buildAllBenefits(isFamily).filterNotNull()

    internal fun buildAllBenefits(isFamily: Boolean) = listOf(
        getBundleType(),
        getStoringPasswordLimit(),
        getDevicesSyncLimit(),
        getAutofill(),
        getSecurityAlerts(),
        getWifiProtection(isFamily),
        getDocumentStorage(),
        get2FAType(),
        getSharingPasswordLimit(),
        getSecureNotesCreation(),
        getPasswordGenerator()
    )

    private fun getBundleType() =
        capabilities.multipleAccounts?.let { capability ->
            if (capability.enabled) {
                val rawAccountNumber = capability.info?.get(INFO_BUNDLE_ACCOUNT_NUMBER) as? Number
                val accountNumber = rawAccountNumber?.toInt() ?: return@let null
                val accountTypeResId = R.string.benefit_bundle_account_type_premium
                StringText(
                    stringRes = R.string.benefit_bundle,
                    arg1 = IntArg(accountNumber),
                    arg2 = StringResArg(accountTypeResId)
                )
            } else {
                StringText(R.string.benefit_individual_acount)
            }
        }

    private fun getStoringPasswordLimit() =
        capabilities.passwordsLimit?.let { capability ->
            if (capability.enabled) {
                val rawPasswordLimit = capability.info?.get(INFO_STORE_PASSWORDS_LIMIT) as? Number
                val passwordLimit = rawPasswordLimit?.toInt() ?: return@let null
                StringText(
                    R.string.benefit_store_passwords_limited_arg,
                    StringArg(passwordLimit.toString())
                )
            } else {
                StringText(R.string.benefit_store_passwords_unlimited)
            }
        }

    private fun getDevicesSyncLimit() =
        capabilities.devicesLimit?.let { capability ->
            if (capability.enabled) {
                val rawDeviceLimit = capability.info?.get(INFO_SYNC_DEVICES_LIMIT) as? Number
                val devicesLimit = rawDeviceLimit?.toInt() ?: return@let null
                PluralsText(R.plurals.benefit_limited_device, devicesLimit, IntArg(devicesLimit))
            } else {
                StringText(R.string.benefit_unlimited_devices)
            }
        }

    private fun getAutofill() = StringText(R.string.benefit_autofill)

    private fun getSecurityAlerts() =
        if (capabilities.securityBreach?.enabled == true && capabilities.dataLeak?.enabled == true) {
            StringText(R.string.benefit_security_alerts_advanced)
        } else if (capabilities.securityBreach?.enabled == true) {
            StringText(R.string.benefit_security_alerts_basic)
        } else {
            null
        }

    private fun getWifiProtection(isFamily: Boolean) =
        capabilities.secureWiFi
            ?.takeIf { it.enabled && userFeaturesChecker.canShowVpn() }
            ?.let {
                if (isFamily) {
                    StringText(R.string.benefit_vpn_family)
                } else {
                    StringText(R.string.benefit_vpn)
                }
            }

    private fun getDocumentStorage() =
        capabilities.secureFiles?.takeIf { it.enabled }?.let { capability ->
            val quotaInfo = capability.info?.get(INFO_SECURE_FILES_QUOTA) as? Map<*, *>?
            val maxQuotaInBytes =
                quotaInfo?.get(INFO_SECURE_FILES_QUOTA_MAX) as? Number ?: return@let null
            val maxQuotaInGb = maxQuotaInBytes.toDouble().div(2.0.pow(30))
            val formattedValue = DecimalFormat("#.##").format(maxQuotaInGb)
            StringText(R.string.benefit_secure_files, StringArg(formattedValue))
        }

    private fun getSharingPasswordLimit() =
        capabilities.sharingLimit?.let { capability ->
            if (capability.enabled) {
                val rawSharingLimit = capability.info?.get(INFO_SHARING_LIMIT) as? Number
                val sharingLimit = rawSharingLimit?.toInt() ?: return@let null
                StringText(
                    R.string.benefit_password_sharing_limited,
                    StringArg(sharingLimit.toString())
                )
            } else {
                StringText(R.string.benefit_password_sharing_unlimited)
            }
        }

    private fun get2FAType() =
        capabilities.yubikey?.let { capability ->
            if (capability.enabled) {
                StringText(R.string.benefit_2fa_advanced)
            } else {
                StringText(R.string.benefit_2fa_basic)
            }
        }

    private fun getSecureNotesCreation() =
        capabilities.secureNotes?.takeIf { it.enabled }
            ?.let { StringText(R.string.benefit_secure_notes) }

    private fun getPasswordGenerator() = StringText(R.string.benefit_password_generator)

    companion object {
        internal const val INFO_BUNDLE_ACCOUNT_NUMBER = "limit"

        internal const val INFO_STORE_PASSWORDS_LIMIT = "limit"

        internal const val INFO_SYNC_DEVICES_LIMIT = "limit"

        internal const val INFO_SECURE_FILES_QUOTA = "quota"
        internal const val INFO_SECURE_FILES_QUOTA_MAX = "max"

        internal const val INFO_SHARING_LIMIT = "limit"
    }
}