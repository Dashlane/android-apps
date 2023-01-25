package com.dashlane.autofill.core

import com.dashlane.autofill.api.changepassword.AutofillChangePasswordLogger
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.events.anonymous.ChangePasswordGuidedAnonymous
import com.dashlane.hermes.generated.events.user.ChangePasswordGuided
import com.dashlane.useractivity.hermes.TrackingLogUtils
import javax.inject.Inject

class AutofillChangePasswordLoggerImpl @Inject constructor(
    private val hermesLogRepository: LogRepository
) : AutofillChangePasswordLogger {

    override var domain: String? = null
    override var packageName: String? = null
    private val loggerDomain =
        TrackingLogUtils.createDomainForLog(domain, packageName)

    override fun logUpdate(id: String) {
        hermesLogRepository.queueEvent(ChangePasswordGuided(FlowStep.COMPLETE, ItemId(id = id)))
        hermesLogRepository.queueEvent(ChangePasswordGuidedAnonymous(FlowStep.COMPLETE, loggerDomain))
    }

    override fun logCancel(id: String) {
        hermesLogRepository.queueEvent(ChangePasswordGuided(FlowStep.CANCEL, ItemId(id = id)))
        hermesLogRepository.queueEvent(ChangePasswordGuidedAnonymous(FlowStep.CANCEL, loggerDomain))
    }

    override fun logOnClickUpdateAccount(id: String) {
        hermesLogRepository.queueEvent(ChangePasswordGuided(FlowStep.START, ItemId(id = id)))
        hermesLogRepository.queueEvent(ChangePasswordGuidedAnonymous(FlowStep.START, loggerDomain))
    }
}