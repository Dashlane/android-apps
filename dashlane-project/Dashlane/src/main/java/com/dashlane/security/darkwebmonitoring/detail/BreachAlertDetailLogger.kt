package com.dashlane.security.darkwebmonitoring.detail

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.events.user.DismissSecurityAlert
import com.dashlane.security.getAlertTypeForLogs
import com.dashlane.security.getItemTypesForLogs
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import javax.inject.Inject

class BreachAlertDetailLogger @Inject constructor(
    private val hermesLogRepository: LogRepository,
) : BreachAlertDetail.Logger {

    override fun logDelete(breachWrapper: BreachWrapper) {
        val breach = breachWrapper.publicBreach
        hermesLogRepository.queueEvent(
            DismissSecurityAlert(
                itemTypesAffected = breach.getItemTypesForLogs(),
                securityAlertType = breach.getAlertTypeForLogs(),
                securityAlertItemId = ItemId(breach.id)
            )
        )
    }
}
