package com.dashlane.premium.enddate

import java.time.LocalDate

sealed class FormattedEndDate {
    object None : FormattedEndDate()
    object Lifetime : FormattedEndDate()
    data class DaysLeftCountdown(val daysLeft: Long) : FormattedEndDate()
    data class Expiration(val expirationDate: LocalDate) : FormattedEndDate()
    data class RenewOn(val renewDate: LocalDate) : FormattedEndDate()
    data class RenewEvery(val periodicity: Periodicity) : FormattedEndDate()
    enum class Periodicity { MONTHLY, YEARLY, UNDEFINED }
    enum class Trigger { MANUAL, AUTOMATIC }
}