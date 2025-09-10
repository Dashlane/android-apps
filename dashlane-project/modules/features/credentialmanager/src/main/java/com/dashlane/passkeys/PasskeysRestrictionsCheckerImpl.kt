package com.dashlane.passkeys

import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm
import com.dashlane.storage.userdata.accessor.filter.PasskeyFilter
import com.dashlane.storage.userdata.restriction.PasskeysRestrictionsChecker
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class PasskeysRestrictionsCheckerImpl @Inject constructor(
    private val featuresChecker: UserFeaturesChecker,
) : PasskeysRestrictionsChecker {
    override fun canUseCloudPasskeys(): Boolean = featuresChecker.has(FeatureFlip.CLOUD_PASSKEYS)

    override fun canUsePasskey(
        passkeySummary: SummaryObject.Passkey,
        passkeyFilter: PasskeyFilter,
    ): Boolean {
        val passkeyAlgorithm = passkeySummary.keyAlgorithm?.let { PasskeyAlgorithm.provider(it) }
        return when (passkeyAlgorithm) {
            PasskeyAlgorithm.Cloud -> passkeyFilter.allowsCloudPasskeys && canUseCloudPasskeys()
            PasskeyAlgorithm.Local.ES256 -> true
            null -> false
        }
    }
}