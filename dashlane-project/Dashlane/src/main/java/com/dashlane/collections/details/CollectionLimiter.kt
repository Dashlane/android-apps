package com.dashlane.collections.details

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.isCurrentTeamTrial
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.featureflipping.UserFeaturesChecker
import javax.inject.Inject

class CollectionLimiter @Inject constructor(
    private val userFeatureChecker: UserFeaturesChecker,
    private val sharingDataProvider: SharingDataProvider,
    private val accountStatusRepository: AccountStatusRepository,
    private val sessionManager: SessionManager
) {

    private val session: Session?
        get() = sessionManager.session

    private val hasCollectionSharing: Boolean
        get() = userFeatureChecker.has(PremiumStatus.PremiumCapability.Capability.COLLECTIONSHARING)

    private val info: PremiumStatus.PremiumCapability.Info?
        get() = userFeatureChecker.getCapabilityInfo(
            PremiumStatus.PremiumCapability.Capability.COLLECTIONSHARING
        )

    private val isCurrentTeamTrial: Boolean
        get() = accountStatusRepository[session]?.premiumStatus?.isCurrentTeamTrial == true

    private val reason: PremiumStatus.PremiumCapability.Info.Reason?
        get() = info?.reason

    private val limit: Long
        get() = info?.limit ?: 0

    private val whoCanShare: PremiumStatus.PremiumCapability.Info.WhoCanShare?
        get() = info?.whoCanShare

    enum class UserLimit {
        DISABLED, 
        NOT_ADMIN, 
        NO_LIMIT, 
        BUSINESS_TRIAL, 
        APPROACHING_LIMIT, 
        REACHED_LIMIT, 
        REACHED_LIMIT_EXISTING_ITEM, 
    }

    suspend fun checkCollectionLimit(isShared: Boolean = false): UserLimit {
        if (!hasCollectionSharing) {
            return if (reason == PremiumStatus.PremiumCapability.Info.Reason.NOT_ADMIN) {
                UserLimit.NOT_ADMIN
            } else {
                UserLimit.DISABLED
            }
        }
        return when {
            isCurrentTeamTrial -> UserLimit.BUSINESS_TRIAL
            whoCanShare == PremiumStatus.PremiumCapability.Info.WhoCanShare.EVERYONE -> UserLimit.NO_LIMIT
            else -> {
                val count = sharingDataProvider.getAcceptedCollections(false).size
                if (count < limit) {
                    UserLimit.APPROACHING_LIMIT
                } else {
                    if (isShared) {
                        UserLimit.REACHED_LIMIT_EXISTING_ITEM
                    } else {
                        UserLimit.REACHED_LIMIT
                    }
                }
            }
        }
    }
}
