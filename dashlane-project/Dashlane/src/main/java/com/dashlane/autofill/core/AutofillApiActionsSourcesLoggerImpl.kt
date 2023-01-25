package com.dashlane.autofill.core

import com.dashlane.autofill.api.actionssources.AutofillActionsSourcesLogger
import com.dashlane.autofill.api.actionssources.model.ActionedFormSource
import com.dashlane.autofill.api.util.formSourceIdentifier
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillApiActionsSourcesLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : AutofillActionsSourcesLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun showList(allFormSources: List<ActionedFormSource>) {
        log(
            UsageLogCode35(
                action = UL35_PAUSED_AND_LINKED_ACTION,
                subaction = UL35_VIEW_PAUSED_AND_LINKED_SUB_ACTION,
                type = UL35_PAUSED_AND_LINKED_TYPE,
                nbOfItems = allFormSources.size
            )
        )
    }

    override fun clickItem(autoFillFormSource: AutoFillFormSource, numberOfItemsInList: Int) {
        log(
            UsageLogCode35(
                action = UL35_PAUSED_AND_LINKED_ACTION,
                subaction = UL35_CLICK_PAUSED_AND_LINKED_ITEM_SUB_ACTION,
                type = UL35_PAUSED_AND_LINKED_TYPE,
                nbOfItems = numberOfItemsInList,
                website = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is WebDomainFormSource },
                appId = autoFillFormSource.formSourceIdentifier.takeIf { autoFillFormSource is ApplicationFormSource }
            )
        )
    }

    companion object {
        private const val UL35_PAUSED_AND_LINKED_ACTION = "managePausedAndLinked"
        private const val UL35_PAUSED_AND_LINKED_TYPE = "settings"
        private const val UL35_VIEW_PAUSED_AND_LINKED_SUB_ACTION = "view"
        private const val UL35_CLICK_PAUSED_AND_LINKED_ITEM_SUB_ACTION = "click"
    }
}
