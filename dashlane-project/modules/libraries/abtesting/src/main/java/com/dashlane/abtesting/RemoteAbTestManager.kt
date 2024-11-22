package com.dashlane.abtesting

import androidx.annotation.VisibleForTesting
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.abtesting.AbTestingUserExperimentsService
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class RemoteAbTestManager @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val preferencesManager: PreferencesManager,
    private val service: AbTestingUserExperimentsService,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : RemoteConfiguration {
    private val refreshMutex = Mutex()

    private fun getLastSuccessfulRefreshTime(username: String): Long = preferencesManager[username].getLong(AB_TEST_LAST_REFRESH, 0)

    private fun setLastSuccessfulRefreshTime(username: String, value: Long) {
        preferencesManager[username].putLong(AB_TEST_LAST_REFRESH, value)
    }

    private fun hasRefreshed(username: String): Boolean {
        return getLastSuccessfulRefreshTime(username) > 0L
    }

    private fun shouldSkipRefresh(username: String): Boolean {
        val lastSuccessfulRefreshTime = getLastSuccessfulRefreshTime(username)
        return lastSuccessfulRefreshTime > 0L && (System.currentTimeMillis() - lastSuccessfulRefreshTime) < REFRESH_DELAY_MILLIS
    }

    override fun launchRefreshIfNeeded(authorization: Authorization.User) {
        applicationCoroutineScope.launch {
            refreshIfNeeded(authorization)
        }
    }

    override suspend fun refreshIfNeeded(authorization: Authorization.User) {
        refreshMutex.withLock {
            if (shouldSkipRefresh(authorization.login)) return

            runCatching {
                refresh(authorization)
            }
        }
    }

    override fun load(username: String): RemoteConfiguration.LoadResult {
        return if (hasRefreshed(username)) RemoteConfiguration.LoadResult.Success else RemoteConfiguration.LoadResult.Failure
    }

    private suspend fun refresh(authorization: Authorization.User) = withContext(defaultDispatcher) {
        
        if (ALL_TESTS.isEmpty()) {
            setLastSuccessfulRefreshTime(authorization.login, System.currentTimeMillis())
            return@withContext
        }
        val request = AbTestingUserExperimentsService.Request(ALL_TESTS.map { it.name })

        
        val response = service.execute(authorization, request)
        val content = response.data.abTests

        
        val abTestStatus = prepareUpdateList(content, ALL_TESTS)
        updateAbPreferences(authorization.login, abTestStatus)

        setLastSuccessfulRefreshTime(authorization.login, System.currentTimeMillis())
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
    internal fun updateAbPreferences(username: String, updatedValues: List<AbTestStatus>): Boolean {
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
        
        return preferencesManager[username].apply(preferencesEntryList)
    }

    private fun getVersion(username: String, test: Test): Int? {
        val versionPreference = test.name.asVersionPreferenceName()
        return preferencesManager[username].getString(versionPreference, null)?.toIntOrNull()
    }

    fun getVariant(username: String, test: Test): Variant? {
        val variantPreference = test.name.asVariantPreferenceName()
        return preferencesManager[username].getString(variantPreference, null)?.let { variantName ->
            test.variants.firstOrNull { it.name == variantName }
        }
    }

    fun getVariantForUser(test: Test, username: Username): Variant? {
        val variantPreference = test.name.asVariantPreferenceName()
        return preferencesManager[username].getString(variantPreference, null)?.let { variantName ->
            test.variants.firstOrNull { it.name == variantName }
        }
    }

    fun getAbTestStatus(username: String, test: Test): AbTestStatus {
        val variant = getVariant(username, test)
        val version = getVersion(username, test)
        return AbTestStatus(test.name, variant?.name, version)
    }

    fun getAbTestStatus(username: String, tests: List<Test>): List<AbTestStatus> {
        return tests.map { test ->
            getAbTestStatus(username, test)
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
