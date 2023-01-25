package com.dashlane.ui.fab

import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository

class FabMenuItemNavigatorLogger(private val usageLogRepository: UsageLogRepository) {

    fun log(
        subType: String?,
        action: String?,
        subAction: UsageLogCode11.Type?
    ) {
        usageLogRepository.enqueue(
            UsageLogCode75(
                type = UsageLogConstant.FabType.first_action_button,
                subtype = subType,
                action = action,
                subaction = subAction?.code
            )
        )
    }
}