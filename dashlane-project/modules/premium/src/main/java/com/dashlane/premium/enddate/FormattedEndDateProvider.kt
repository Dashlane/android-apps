package com.dashlane.premium.enddate

import java.time.Instant

interface FormattedEndDateProvider {
    val isLegacy: Boolean
    val isTrial: Boolean
    val endDate: Instant?
    fun getFormattedAutoRenewTrigger(): FormattedEndDate.Trigger?
    fun getFormattedAutoRenewPeriodicity(): FormattedEndDate.Periodicity
    fun willAutoRenew(): Boolean
    fun hasLifetimeEntitlement(): Boolean
}