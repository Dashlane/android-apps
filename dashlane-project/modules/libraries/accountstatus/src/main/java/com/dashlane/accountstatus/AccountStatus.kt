package com.dashlane.accountstatus

import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.google.gson.annotations.SerializedName

data class AccountStatus(
    @SerializedName("premiumStatus")
    val premiumStatus: PremiumStatus,
    @SerializedName("subscriptionInfo")
    val subscriptionInfo: SubscriptionInfo
)
