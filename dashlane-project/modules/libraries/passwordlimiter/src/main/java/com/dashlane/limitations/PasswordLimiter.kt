package com.dashlane.limitations

import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.filter.counterFilter
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class PasswordLimiter @Inject constructor(
    private val dataCounter: DataCounter,
    private val userFeatureChecker: UserFeaturesChecker
) {
    val hasPasswordLimit: Boolean
        get() = userFeatureChecker.has(PremiumStatus.Capabilitie.Capability.PASSWORDSLIMIT)

    val passwordLimitCount: Long?
        get() = userFeatureChecker.getCapabilityInfo(PremiumStatus.Capabilitie.Capability.PASSWORDSLIMIT)?.limit

    enum class UserLimit {
        USER_NO_LIMIT,
        USER_APPROACHING_LIMIT,
        USER_LIMIT_REACHED;
    }

    fun isPasswordLimitReached() = checkUserLimit() == UserLimit.USER_LIMIT_REACHED

    fun passwordRemainingBeforeLimit(): Long {
        if (checkUserLimit() != UserLimit.USER_NO_LIMIT) {
            return passwordLimitCount?.minus(getCountAuthentifiant()) ?: 0
        }
        return -1
    }

    private fun checkUserLimit(): UserLimit {
        val passwordLimit = passwordLimitCount
        if (!hasPasswordLimit || passwordLimit == null) {
            
            return UserLimit.USER_NO_LIMIT
        }
        val nbPasswordsSaved = getCountAuthentifiant()
        return when {
            nbPasswordsSaved >= passwordLimit -> UserLimit.USER_LIMIT_REACHED
            nbPasswordsSaved + PASSWORD_LIMIT_LOOMING >= passwordLimit -> UserLimit.USER_APPROACHING_LIMIT
            else -> UserLimit.USER_NO_LIMIT
        }
    }

    fun getCountAuthentifiant() = dataCounter.count(
        counterFilter {
            ignoreUserLock()
            specificDataType(SyncObjectType.AUTHENTIFIANT)
        }
    )

    companion object {
        const val PASSWORD_LIMIT_LOOMING = 5
    }
}
