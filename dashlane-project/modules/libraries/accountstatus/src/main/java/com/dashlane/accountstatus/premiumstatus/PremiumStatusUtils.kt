package com.dashlane.accountstatus.premiumstatus

import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.CurrentTeam
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.PastTeam
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2cStatus.PlanFeature
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2cStatus.StatusCode
import com.dashlane.server.api.time.toInstant
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime


const val LIFETIME_THRESHOLD_IN_YEARS = 65L

fun PremiumStatus.remainingDays(clock: Clock): Long? {
    val endDate = b2cStatus.endDateUnix?.toInstant() ?: return null
    return Duration.between(clock.instant(), endDate).toDays()
}

val PremiumStatus.autoRenewal: Boolean
    get() = b2cStatus.autoRenewal

val PremiumStatus.familyStatus: PremiumStatus.B2cStatus.FamilyStatus?
    get() = b2cStatus.familyStatus

val PremiumStatus.planName: String?
    get() = b2cStatus.planName

val PremiumStatus.planType: PremiumStatus.B2cStatus.PlanType?
    get() = b2cStatus.planType

val PremiumStatus.planFeature: PlanFeature?
    get() = b2cStatus.planFeature

val PremiumStatus.isPremium: Boolean
    get() = b2cStatus.statusCode == StatusCode.SUBSCRIBED

fun PremiumStatus.hasLifetimeEntitlement(clock: Clock): Boolean {
    val endDateDate = b2cStatus.endDateUnix?.let {
        LocalDateTime.ofInstant(it.toInstant(), clock.zone)
    } ?: return false

    val lifetimeThresholdDate = LocalDateTime.now(clock)
        .plusYears(LIFETIME_THRESHOLD_IN_YEARS)

    return endDateDate.isAfter(lifetimeThresholdDate)
}

val PremiumStatus.isFamilyPlan: Boolean
    get() = b2cStatus.familyStatus != null

val PremiumStatus.isAdvancedPlan: Boolean
    get() = b2cStatus.planFeature == PlanFeature.ESSENTIALS

val PremiumStatus.isPremiumPlan: Boolean
    get() = b2cStatus.planFeature == PlanFeature.PREMIUM

val PremiumStatus.isPremiumPlusPlan: Boolean
    get() = b2cStatus.planFeature == PlanFeature.PREMIUMPLUS

val PremiumStatus.isFamilyAdmin: Boolean
    get() = b2cStatus.familyStatus?.isAdmin == true

val PremiumStatus.isLegacy: Boolean
    get() = b2cStatus.statusCode == StatusCode.LEGACY

val PremiumStatus.isTrial: Boolean
    get() = b2cStatus.isTrial

val PremiumStatus.isFree: Boolean
    get() = b2cStatus.statusCode == StatusCode.FREE

val PremiumStatus.currentTeam: CurrentTeam?
    get() = b2bStatus?.currentTeam

val PremiumStatus.isCurrentTeamTrial: Boolean
    get() = currentTeam?.isTrial == true

val PremiumStatus.pastTeams: List<PastTeam>?
    get() = b2bStatus?.pastTeams

val PremiumStatus.endDate: Instant?
    get() = b2cStatus.endDateUnix?.toInstant()