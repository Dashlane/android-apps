package com.dashlane.accountstatus

import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.SubscriptionInfo
import com.google.gson.annotations.SerializedName

data class AccountStatus(
    @SerializedName("premiumStatus")
    val premiumStatus: PremiumStatus,
    @SerializedName("subscriptionInfo")
    val subscriptionInfo: SubscriptionInfo,
)

fun AccountStatus.getB2bStatus(): PremiumStatus.B2bStatus? = this.premiumStatus.b2bStatus

fun AccountStatus.isB2bUser(): Boolean = getB2bStatus()?.currentTeam != null

fun AccountStatus.hasEnforcedTeamSpace(): Boolean = getB2bStatus()?.currentTeam?.teamInfo?.personalSpaceEnabled == false

fun AccountStatus.isVaultExportEnabled(): Boolean = getB2bStatus()?.currentTeam?.teamInfo?.vaultExportEnabled != false