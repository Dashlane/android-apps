package com.dashlane.ui.screens.fragments.userdata

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode13

class ChangeSortAlgorithmLogger(private val usageLogRepository: UsageLogRepository?) {

    fun log(algorithm: String?) {
        algorithm ?: return
        usageLogRepository?.enqueue(
            UsageLogCode13(
                key = "CREDENTIALS_SORT_CRITERIA",
                value = algorithm
            )
        )
    }
}