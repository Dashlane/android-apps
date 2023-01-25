package com.dashlane.abtesting

import androidx.annotation.VisibleForTesting
import com.dashlane.network.tools.authorization
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.abtesting.AbTestingUserExperimentsService
import com.dashlane.session.RemoteConfiguration
import com.dashlane.session.SessionManager
import com.dashlane.session.Username
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAbTestManager @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val userPreferences: UserPreferencesManager,
    private val service: AbTestingUserExperimentsService
) : RemoteConfiguration {
    private val refreshMutex = Mutex()

    private var lastSuccessfulRefreshTime: Long
        get() = userPreferences.getLong(AB_TEST_LAST_REFRESH, 0)
        set(value) {
            userPreferences.putLong(AB_TEST_LAST_REFRESH, value)
        }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val refreshActor = globalCoroutineScope.actor<Unit>(capacity = Channel.CONFLATED) {
        consumeEach {
            refreshIfNeeded()
        }
    }

    private fun hasRefreshed(): Boolean {
        return lastSuccessfulRefreshTime > 0L
    }

    private fun shouldSkipRefresh(): Boolean {
        return hasRefreshed() && hasRefreshedRecently()
    }

    private fun hasRefreshedRecently() = (System.currentTimeMillis() - lastSuccessfulRefreshTime) < REFRESH_DELAY_MILLIS

    override fun launchRefreshIfNeeded() {
        refreshActor.trySend(Unit)
    }

    override suspend fun refreshIfNeeded() {
        refreshMutex.withLock {
            if (shouldSkipRefresh()) return

            runCatching {
                refresh()
            }
        }
    }

    

    override fun load(): RemoteConfiguration.LoadResult {
        return if (hasRefreshed()) RemoteConfiguration.LoadResult.Success else RemoteConfiguration.LoadResult.Failure
    }

    

    private suspend fun refresh() = withContext(Dispatchers.Default) {
        
        val session = sessionManager.session ?: return@withContext

        
        if (ALL_TESTS.isEmpty()) {
            lastSuccessfulRefreshTime = System.currentTimeMillis()
            return@withContext
        }
        val request = AbTestingUserExperimentsService.Request(ALL_TESTS.map { it.name })

        
        val response = service.execute(session.authorization, request)

        
        if (session != sessionManager.session) return@withContext

        val content = response.data.abTests

        
        val abTestStatus = prepareUpdateList(content, ALL_TESTS)
        updateAbPreferences(abTestStatus)

        lastSuccessfulRefreshTime = System.currentTimeMillis()
    }

    

    @VisibleForTesting
    internal fun prepareUpdateList(
        content: List<AbTestingUserExperimentsService.Data.AbTest>,
        allKnownTests: Array<Test>
    ): List<AbTestStatus> {
        return allKnownTests.map { currentTest ->
            getVariantIfEligibleForTest(currentTest, content)
        }
    }

    

    private fun getVariantIfEligibleForTest(
        currentTest: Test,
        content: List<AbTestingUserExperimentsService.Data.AbTest>
    ):
            AbTestStatus {
        val resultForThisTest = content.firstOrNull { it.name == currentTest.name }
            ?: return AbTestStatus(
                currentTest.name,
                null,
                null
            )
        val availableVariants = currentTest.variants.map { it.name }

        return if (availableVariants.contains(resultForThisTest.variant)) {
            AbTestStatus(
                currentTest.name,
                resultForThisTest.variant,
                resultForThisTest.version.toInt()
            )
        } else {
            AbTestStatus(
                currentTest.name,
                null,
                null
            )
        }
    }

    

    @VisibleForTesting
    internal fun updateAbPreferences(updatedValues: List<AbTestStatus>): Boolean {
        val preferencesEntryList = arrayListOf<PreferenceEntry>()
        updatedValues.onEach {
            val variantKey = it.name.asVariantPreferenceName()
            val variant = it.variant
            val versionKey = it.name.asVersionPreferenceName()
            val version = it.version?.toString()

            if (variant == null) {
                preferencesEntryList.add(PreferenceEntry.toRemove(variantKey))
                preferencesEntryList.add(PreferenceEntry.toRemove(versionKey))
            } else {
                preferencesEntryList.add(PreferenceEntry.setString(variantKey, variant))
                preferencesEntryList.add(PreferenceEntry.setString(versionKey, version))
            }
        }
        
        return userPreferences.apply(preferencesEntryList)
    }

    private fun getVersion(test: Test): Int? {
        val versionPreference = test.name.asVersionPreferenceName()
        return userPreferences.getString(versionPreference, null)?.toIntOrNull()
    }

    fun getVariant(test: Test): Variant? {
        val variantPreference = test.name.asVariantPreferenceName()
        return userPreferences.getString(variantPreference, null)?.let { variantName ->
            test.variants.firstOrNull { it.name == variantName }
        }
    }

    fun getVariantForUser(test: Test, username: Username): Variant? {
        val variantPreference = test.name.asVariantPreferenceName()
        return userPreferences.preferencesFor(username).getString(variantPreference, null)?.let { variantName ->
            test.variants.firstOrNull { it.name == variantName }
        }
    }

    fun getAbTestStatus(test: Test): AbTestStatus {
        val variant = getVariant(test)
        val version = getVersion(test)
        return AbTestStatus(test.name, variant?.name, version)
    }

    fun getAbTestStatus(tests: List<Test>): List<AbTestStatus> {
        return tests.map { test ->
            getAbTestStatus(test)
        }
    }

    private fun String.asVariantPreferenceName(): String {
        return AB_TEST_VARIANT_PREFIX + this
    }

    private fun String.asVersionPreferenceName(): String {
        return AB_TEST_VERSION_PREFIX + this
    }

    class Test(name: String, val variants: Array<Variant>) : AbTest(name)

    companion object {
        private val REFRESH_DELAY_MILLIS = TimeUnit.DAYS.toMillis(1)

        const val AB_TEST_LAST_REFRESH = "abtest_last_refresh"
        const val AB_TEST_VARIANT_PREFIX = "abtest_variant_"
        const val AB_TEST_VERSION_PREFIX = "abtest_version_"

        private val ALL_TESTS = arrayOf<Test>()
    }
}
