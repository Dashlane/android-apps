package com.dashlane.premium.enddate

import android.content.res.Resources
import com.dashlane.premium.R
import com.dashlane.premium.enddate.FormattedEndDate.DaysLeftCountdown
import com.dashlane.premium.enddate.FormattedEndDate.Expiration
import com.dashlane.premium.enddate.FormattedEndDate.Lifetime
import com.dashlane.premium.enddate.FormattedEndDate.None
import com.dashlane.premium.enddate.FormattedEndDate.Periodicity
import com.dashlane.premium.enddate.FormattedEndDate.RenewEvery
import com.dashlane.premium.enddate.FormattedEndDate.RenewOn
import com.dashlane.premium.enddate.FormattedEndDate.Trigger
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class EndDateFormatter @Inject constructor(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val resources: Resources
) {
    fun getLabel(
        provider: FormattedEndDateProvider,
        noEndDateLabel: String? = null
    ): String? =
        when (val formattedEndDate = getType(provider)) {
            None -> noEndDateLabel
            Lifetime -> resources.getString(R.string.lifetime_plan)
            is DaysLeftCountdown -> {
                val daysLeft = formattedEndDate.daysLeft.toInt()
                resources.getQuantityString(R.plurals.plan_days_left, daysLeft, daysLeft.toString())
            }
            is Expiration -> {
                val date = formatDate(formattedEndDate.expirationDate)
                resources.getString(R.string.plan_expires_on, date)
            }
            is RenewOn -> {
                val date = formatDate(formattedEndDate.renewDate)
                resources.getString(R.string.plan_renews_on, date)
            }
            is RenewEvery ->
                when (formattedEndDate.periodicity) {
                    Periodicity.MONTHLY -> resources.getString(R.string.plan_renews_monthly)
                    Periodicity.YEARLY -> resources.getString(R.string.plan_renews_yearly)
                    Periodicity.UNDEFINED -> resources.getString(R.string.plan_renews_generic)
                }
        }

    private fun getType(provider: FormattedEndDateProvider): FormattedEndDate {
        if (provider.isLegacy) {
            return None
        }
        val endDateAsInstant = provider.endDate ?: return None
        val endDate = LocalDateTime.ofInstant(endDateAsInstant, ZoneOffset.UTC)
        val timeLeft = Duration.between(LocalDateTime.now(clock), endDate)

        return when {
            hasInvalidEndDate(endDateAsInstant, timeLeft) ->
                None
            provider.isTrial ->
                DaysLeftCountdown(timeLeft.toDays())
            !provider.willAutoRenew() && provider.hasLifetimeEntitlement() ->
                Lifetime
            !provider.willAutoRenew() ->
                Expiration(endDate.toLocalDate())
            provider.getFormattedAutoRenewTrigger() == Trigger.MANUAL ->
                RenewOn(endDate.toLocalDate())
            provider.getFormattedAutoRenewTrigger() == Trigger.AUTOMATIC ->
                RenewEvery(provider.getFormattedAutoRenewPeriodicity())
            else ->
                None
        }
    }

    private fun hasInvalidEndDate(endDateAsInstant: Instant, timeLeft: Duration) =
        endDateAsInstant === Instant.EPOCH || timeLeft.isNegative || timeLeft.isZero

    private fun formatDate(date: LocalDate): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date)
}