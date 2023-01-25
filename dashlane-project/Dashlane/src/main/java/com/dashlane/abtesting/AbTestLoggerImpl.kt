package com.dashlane.abtesting

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode132
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.Username



class AbTestLoggerImpl(
    private val usageLogRepository: UsageLogRepository?,
    private val preferencesManager: UserPreferencesManager
) : AbTestLogger {
    override fun logVariant(
        status: AbTestStatus,
        isPriorityLog: Boolean
    ): Boolean {
        return if (status.version != null && status.variant != null) {
            log132(status.name, status.version, status.variant, isPriorityLog)
            true
        } else {
            false
        }
    }

    override fun logVariants(
        list: List<AbTestStatus>
    ): Boolean {
        var successReport = true
        list.forEach {
            successReport = logVariant(it) && successReport
        }
        return successReport
    }

    override fun logVariantOnce(
        username: Username,
        abTestStatus: AbTestStatus,
        isPriorityLog: Boolean
    ): Boolean {
        val isTestLogged = isTestLogged(username, abTestStatus)
        return if (!isTestLogged) {
            val result = logVariant(abTestStatus, isPriorityLog)
            if (result) {
                saveLoggedTest(username, abTestStatus)
            }
            result
        } else {
            false
        }
    }

    private fun log132(
        id: String,
        version: Int,
        variantName: String,
        isPriorityLog: Boolean
    ) {
        usageLogRepository?.enqueue(
            UsageLogCode132(
                experimentId = id,
                versionId = version,
                variantId = variantName
            ),
            isPriorityLog
        )
    }
    private fun isTestLogged(username: Username, abTest: AbTestStatus) =
        preferencesManager.preferencesFor(username).getBoolean(abTest.getLogKey(), false)

    private fun saveLoggedTest(
        username: Username,
        abTest: AbTestStatus
    ) {
        val userPreferencesManager = preferencesManager.preferencesFor(username)
        userPreferencesManager.apply(
            PreferenceEntry.setBoolean(abTest.getLogKey(), true)
        )
    }

    private fun AbTestStatus.getLogKey(): String {
        return AB_TEST_LOGGED_PREFIX + name
    }

    companion object {
        const val AB_TEST_LOGGED_PREFIX = "ab_test_logged_once"
    }
}