package com.dashlane.frozenaccount

import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability.PASSWORDSLIMIT
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Info.Action.ENFORCE_FREEZE
import javax.inject.Inject

interface FrozenStateManager {
    val isAccountFrozen: Boolean
    val passwordLimitCount: Long?
}

class FrozenStateManagerImpl @Inject constructor(
    private val passwordLimiter: PasswordLimiter,
    private val userFeaturesChecker: UserFeaturesChecker
) : FrozenStateManager {
    override val isAccountFrozen: Boolean
        get() = passwordLimiter.isPasswordLimitExceeded() && isUserFrozenOnLimitBreached()

    override val passwordLimitCount: Long?
        get() = passwordLimiter.passwordLimitCount

    private fun isUserFrozenOnLimitBreached(): Boolean {
        return userFeaturesChecker.has(PASSWORDSLIMIT) &&
                userFeaturesChecker.getCapabilityInfo(PASSWORDSLIMIT)?.action == ENFORCE_FREEZE
    }
}
