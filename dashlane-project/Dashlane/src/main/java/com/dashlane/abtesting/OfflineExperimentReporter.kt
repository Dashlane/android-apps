package com.dashlane.abtesting

import com.dashlane.network.tools.authorization
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.abtesting.AbTestingOfflineExperimentReportingService
import com.dashlane.server.api.time.toInstantEpochSecond
import com.dashlane.session.SessionManager
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
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
    private val sessionManager: SessionManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val service: AbTestingOfflineExperimentReportingService
) {
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val refreshActor = applicationCoroutineScope.actor<Unit>(capacity = Channel.CONFLATED) {
        consumeEach {
            reportIfNeeded()
        }
    }

    fun launchReportIfNeeded() {
        refreshActor.trySend(Unit)
    }

    suspend fun reportIfNeeded() {
        val abTests = getAbTestInfo(LocalAbTest.values())
        if (!isReportingNeeded() || abTests.isEmpty()) return
        val session = sessionManager.session ?: return
        val request = AbTestingOfflineExperimentReportingService.Request(abTests = abTests.toList())

        
        try {
            
            service.execute(session.authorization, request)
            setReportingSuccessful()
        } catch (t: Throwable) {
        }
    }

    private fun isReportingNeeded() = !userPreferencesManager.getBoolean(PREFERENCE_LOCAL_EXPERIMENT_REPORT)

    private fun setReportingSuccessful() {
        userPreferencesManager.apply(PreferenceEntry.setBoolean(PREFERENCE_LOCAL_EXPERIMENT_REPORT, true))
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
