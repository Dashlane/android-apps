package com.dashlane.darkweb

import com.dashlane.breach.Breach
import com.dashlane.breach.BreachWithOriginalJson
import com.dashlane.session.authorization
import com.dashlane.server.api.endpoints.darkwebmonitoring.DarkWebMonitoringDeregisterEmailService
import com.dashlane.server.api.endpoints.darkwebmonitoring.DarkWebMonitoringListLeaksService
import com.dashlane.server.api.endpoints.darkwebmonitoring.DarkWebMonitoringListRegistrationsService
import com.dashlane.server.api.endpoints.darkwebmonitoring.DarkWebMonitoringRegisterEmailService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DarkWebMonitoringManager @Inject constructor(
    private val registerEmailService: DarkWebMonitoringRegisterEmailService,
    private val deregisterEmailService: DarkWebMonitoringDeregisterEmailService,
    private val listLeaksService: DarkWebMonitoringListLeaksService,
    private val listRegistrationsService: DarkWebMonitoringListRegistrationsService,
    private val sessionManager: SessionManager,
    private val passwordsAnalysis: DarkWebMonitoringLeakedPasswordsAnalysis,
    private val clock: Clock,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val gson = Gson()
    private var cache: Cache? = null

    suspend fun optIn(email: String): String {
        val session = sessionManager.session ?: return ERROR_UNKNOWN

        val response = try {
            registerEmailService.execute(
                userAuthorization = session.authorization,
                request = DarkWebMonitoringRegisterEmailService.Request(email = email),
            )
        } catch (_: DashlaneApiException) {
            return ERROR_UNKNOWN
        }

        if (response.data.result == RESULT_OK) {
            invalidateCache()
        }

        return response.data.result
    }

    suspend fun optOut(email: String): Boolean {
        val session = sessionManager.session ?: return false

        val response = try {
            deregisterEmailService.execute(
                userAuthorization = session.authorization,
                request = DarkWebMonitoringDeregisterEmailService.Request(email = email),
            )
        } catch (_: DashlaneApiException) {
            return false
        }

        return if (response.data.result == RESULT_OK) {
            invalidateCache()
            true
        } else {
            false
        }
    }

    suspend fun getEmailsWithStatus(): List<DarkWebEmailStatus>? {
        invalidateCacheIfUsernameChanged()
        refreshEmailStatusRemote()
        return cache?.emailsStatus
    }

    suspend fun getBreaches(lastUpdateDate: Long): Pair<Long, List<BreachWithOriginalJson>>? {
        val session = sessionManager.session ?: return null

        val response = try {
            listLeaksService.execute(
                userAuthorization = session.authorization,
                request = DarkWebMonitoringListLeaksService.Request(
                    lastUpdateDate = lastUpdateDate,
                    includeDisabled = true,
                )
            )
        } catch (_: DashlaneApiException) {
            return null
        }

        val allBreaches = response.data.leaks ?: return null
        val allPasswords = response.data.details?.let {
            withContext(defaultDispatcher) {
                passwordsAnalysis.extractPasswordMap(
                    cipheredInfo = it.cipheredInfo,
                    cipheredKey = it.cipheredKey,
                )
            }
        }

        return response.data.lastUpdateDate to allBreaches.map { toBreachWithJson(it, allPasswords) }
    }

    fun invalidateCache() {
        cache = null
    }

    private fun invalidateCacheIfUsernameChanged() {
        val cacheRef = cache ?: return 
        val sessionId = sessionManager.session?.sessionId
        if (sessionId != cacheRef.sessionId) {
            invalidateCache() 
        }
    }

    private suspend fun refreshEmailStatusRemote() {
        val lastRefreshCache = cache?.fetchTimestamp ?: 0
        if (clock.millis() - lastRefreshCache < DELAY_BEFORE_REFRESH) {
            
            return
        }

        val session = sessionManager.session ?: return
        val response = try {
            listRegistrationsService.execute(
                userAuthorization = session.authorization,
            )
        } catch (_: DashlaneApiException) {
            return
        }

        cache = Cache(
            sessionId = session.sessionId,
            emailsStatus = response.data.emails.map(::toDarkWebEmailStatus),
            fetchTimestamp = clock.millis()
        )
    }

    private suspend fun toBreachWithJson(it: DarkWebMonitoringListLeaksService.Data.Leak, passwords: Map<String, List<String>?>?):
        BreachWithOriginalJson = withContext(defaultDispatcher) {
        val breach = it.toBreach()
        val json = gson.toJson(it)
        BreachWithOriginalJson(
            breach = breach,
            json = json,
            passwords = passwords?.get(breach.id)
        )
    }

    private fun DarkWebMonitoringListLeaksService.Data.Leak.toBreach(): Breach =
        Breach(
            id = id,
            breachModelVersion = breachModelVersion.toInt(),
            domains = domains,
            impactedEmails = impactedEmails,
            lastModificationRevision = lastModificationRevision.toInt(),
            leakedData = leakedData,
            status = status,
            announcedDate = announcedDate,
            eventDate = eventDate,
            breachCreationDate = breachCreationDate,
        )

    private fun toDarkWebEmailStatus(it: DarkWebMonitoringListRegistrationsService.Data.Email): DarkWebEmailStatus =
        DarkWebEmailStatus(it.email, it.state)

    private class Cache(
        val sessionId: String,
        val emailsStatus: List<DarkWebEmailStatus>?,
        val fetchTimestamp: Long
    )

    private companion object {
        const val RESULT_OK = "OK"
        const val ERROR_UNKNOWN = "UNKNOWN"
        const val DELAY_BEFORE_REFRESH = 2 * 60 * 1000 
    }
}