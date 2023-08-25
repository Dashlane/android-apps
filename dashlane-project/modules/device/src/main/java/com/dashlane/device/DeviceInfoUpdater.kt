package com.dashlane.device

import com.dashlane.network.webservices.GetCountryService
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoUpdater @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val deviceCountryRepository: DeviceInfoRepositoryImpl,
    private val countryService: GetCountryService
) {

    private var refreshing = false

    @Suppress("kotlin:S6311")
    fun refreshIfNeeded() {
        val refreshTimestamp = deviceCountryRepository.deviceCountryRefreshTimestamp
        val millisSinceLastRefresh = System.currentTimeMillis() - refreshTimestamp
        if (millisSinceLastRefresh >= TimeUnit.DAYS.toMillis(REFRESH_PERIOD_DAYS.toLong())) {
            applicationCoroutineScope.launch(mainCoroutineDispatcher) {
                refresh()
            }
        }
    }

    private suspend fun refresh() {
        if (refreshing) return
        refreshing = true

        val countryContent = try {
            countryService.getCountry().content
        } catch (t: Throwable) {
            return
        } finally {
            refreshing = false
        }

        if (countryContent != null) {
            val country = countryContent.country
            if (country != null) {
                deviceCountryRepository.deviceCountry = country
            }
            deviceCountryRepository.inEuropeanUnion = countryContent.isInEuropeanUnion ?: true
        }
    }

    companion object {
        private const val REFRESH_PERIOD_DAYS = 7
    }
}
