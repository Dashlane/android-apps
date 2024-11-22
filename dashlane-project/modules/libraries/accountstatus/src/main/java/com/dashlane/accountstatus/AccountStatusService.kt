package com.dashlane.accountstatus

import android.util.Log
import com.dashlane.session.authorization
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.dashlane.session.Session
import com.dashlane.util.anonymize
import javax.inject.Inject

interface AccountStatusService {
    suspend fun fetchAccountStatus(session: Session): AccountStatus
}

class AccountStatusServiceImpl @Inject constructor(private val dashlaneApi: DashlaneApi) : AccountStatusService {
    override suspend fun fetchAccountStatus(session: Session): AccountStatus {
        return AccountStatus(
            premiumStatus = fetchPremiumStatus(session),
            subscriptionInfo = fetchSubscriptionInfo(session)
        )
    }

    private suspend fun fetchPremiumStatus(session: Session): PremiumStatus = try {
        dashlaneApi.endpoints.premium.premiumStatusService.execute(session.authorization).data
    } catch (t: Throwable) {
        val throwable = t.anonymize()
        Log.d("ACCOUNT_STATUS", "Failed to Fetch PremiumStatus:\n$throwable")
        throw throwable
    }

    private suspend fun fetchSubscriptionInfo(session: Session): SubscriptionInfo = try {
        dashlaneApi.endpoints.premium.getSubscriptionInfoService.execute(session.authorization).data
    } catch (t: Throwable) {
        val throwable = t.anonymize()
        Log.d("ACCOUNT_STATUS", "Failed to fetch SubscriptionInfo\n$throwable")
        throw throwable
    }
}
