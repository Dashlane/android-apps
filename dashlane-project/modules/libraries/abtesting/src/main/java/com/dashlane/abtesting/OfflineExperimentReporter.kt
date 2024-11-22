package com.dashlane.abtesting

import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.abtesting.AbTestingOfflineExperimentReportingService
import com.dashlane.server.api.time.toInstantEpochSecond
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach

class OfflineExperimentReporter @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val localAbTest: LocalAbTestManager,
    private val preferencesManager: PreferencesManager,
    private val service: AbTestingOfflineExperimentReportingService
) {
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val refreshActor = applicationCoroutineScope.actor(capacity = Channel.CONFLATED) {
        consumeEach {
            reportIfNeeded(it)
        }
    }

    fun launchReportIfNeeded(authorization: Authorization.User) {
        refreshActor.trySend(authorization)
    }

    suspend fun reportIfNeeded(authorization: Authorization.User) {
        val preferences = preferencesManager[authorization.login]
        val abTests = getAbTestInfo(LocalAbTest.entries.toTypedArray())
        if (!preferences.getBoolean(PREFERENCE_LOCAL_EXPERIMENT_REPORT) || abTests.isEmpty()) return
        val request = AbTestingOfflineExperimentReportingService.Request(abTests = abTests.toList())

        
        try {
            
            service.execute(authorization, request)
            preferences.apply(PreferenceEntry.setBoolean(PREFERENCE_LOCAL_EXPERIMENT_REPORT, true))
        } catch (t: Throwable) {
        }
    }

    private fun getAbTestInfo(tests: Array<LocalAbTest>): List<AbTestingOfflineExperimentReportingService.Request.AbTest> {
        return tests.mapNotNull { currentTest ->
            val variant = localAbTest.getStoredVariant(currentTest)
            variant?.let {
                val selectionDateMillis = localAbTest.getSelectionDate(currentTest) ?: System.currentTimeMillis()
                AbTestingOfflineExperimentReportingService.Request.AbTest(
                    currentTest.testName.lowercase(),
                    variant.name,
                    Instant.ofEpochMilli(selectionDateMillis).toInstantEpochSecond()
                )
            }
        }
    }

    companion object {
        private const val PREFERENCE_LOCAL_EXPERIMENT_REPORT = "ab_test_local_experiment_report"
    }
}
