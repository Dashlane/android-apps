package com.dashlane.autofill.core

import com.dashlane.autofill.api.changepause.AutofillApiChangePauseLogger
import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.api.util.formSourceIdentifier
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillApiApiChangePauseLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : AutofillApiChangePauseLogger, AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun resumeFormSource(autoFillFormSource: AutoFillFormSource) {
        log(
            UsageLogCode35(
                action = UL35_OFF_CHANGE_PAUSE_ACTION,
                subaction = UL35_CLICK_CHANGE_PAUSE_SUB_ACTION,
                type = UL35_CHANGE_PAUSE_TYPE,
                website = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is WebDomainFormSource },
                appId = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is ApplicationFormSource }
            )
        )
    }

    override fun openPauseForFormSource(autoFillFormSource: AutoFillFormSource) {
        log(
            UsageLogCode35(
                action = UL35_ON_CHANGE_PAUSE_ACTION,
                subaction = UL35_CLICK_CHANGE_PAUSE_SUB_ACTION,
                type = UL35_CHANGE_PAUSE_TYPE,
                website = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is WebDomainFormSource },
                appId = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is ApplicationFormSource }
            )
        )
    }

    override fun pauseFormSource(autoFillFormSource: AutoFillFormSource, pauseDurations: PauseDurations) {
        val subAction = when (pauseDurations) {
            PauseDurations.ONE_HOUR -> UL35_SHORT_PAUSE_CHANGE_PAUSE_SUB_ACTION
            PauseDurations.ONE_DAY -> UL35_LONG_PAUSE_CHANGE_PAUSE_SUB_ACTION
            PauseDurations.PERMANENT -> UL35_DEFINITE_PAUSE_CHANGE_PAUSE_SUB_ACTION
        }
        log(
            UsageLogCode35(
                action = UL35_ON_CHANGE_PAUSE_ACTION,
                subaction = subAction,
                type = UL35_CHANGE_PAUSE_TYPE,
                website = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is WebDomainFormSource },
                appId = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is ApplicationFormSource }
            )
        )
    }

    companion object {
        private const val UL35_ON_CHANGE_PAUSE_ACTION = "PauseAutofillOn"
        private const val UL35_OFF_CHANGE_PAUSE_ACTION = "PauseAutofillOff"
        private const val UL35_CLICK_CHANGE_PAUSE_SUB_ACTION = "click"
        private const val UL35_SHORT_PAUSE_CHANGE_PAUSE_SUB_ACTION = "shortPause"
        private const val UL35_LONG_PAUSE_CHANGE_PAUSE_SUB_ACTION = "longPause"
        private const val UL35_DEFINITE_PAUSE_CHANGE_PAUSE_SUB_ACTION = "definitePause"
        private const val UL35_CHANGE_PAUSE_TYPE = "settings"
    }
}
