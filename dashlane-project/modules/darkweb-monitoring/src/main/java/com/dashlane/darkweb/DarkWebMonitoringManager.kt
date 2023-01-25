package com.dashlane.darkweb

import com.dashlane.breach.Breach
import com.dashlane.breach.BreachWithOriginalJson
import com.dashlane.network.webservices.DashlaneUrls
import com.dashlane.session.SessionManager
import com.dashlane.util.tryOrNull
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class DarkWebMonitoringManager @Inject constructor(
    callFactory: Call.Factory,
    private val sessionManager: SessionManager,
    private val passwordsAnalysis: DarkWebMonitoringLeakedPasswordsAnalysis
) {
    private val darkWebService = Retrofit.Builder()
        .callFactory(callFactory)
        .baseUrl(DashlaneUrls.URL_WEBSERVICES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DarkWebService::class.java)

    private val gson = Gson()
    private var cache: Cache? = null

    

    suspend fun optIn(email: String): String {
        val session = sessionManager.session ?: return ERROR_UNKNOWN
        val emails = JSONArray().put(email)
        val result = try {
            darkWebService.optIn(session.userId, session.uki, emails).content
        } catch (e: Exception) {
            null
        }
        val resultCode = result?.emailsWithResult?.firstOrNull { it.email == email }?.status ?: ERROR_UNKNOWN
        if (resultCode == "OK") {
            invalidateCache()
            refreshEmailStatusRemote()
        }
        return resultCode
    }

    

    suspend fun optOut(email: String): Boolean {
        val session = sessionManager.session ?: return false
        val emails = JSONArray().put(email)
        val success = try {
            darkWebService.optOut(session.userId, session.uki, emails).code == 200
        } catch (e: Exception) {
            false
        }
        return if (success) {
            invalidateCache() 
            refreshEmailStatusRemote()
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
        val login = session.userId

        val breachesContent = tryOrNull {
            darkWebService.getBreaches(
                login, session.uki,
                includeDisabled = true,
                wantsDetails = true,
                lastUpdateDate = lastUpdateDate
            )
                .content
        } ?: return null
        val allBreaches = breachesContent.breaches ?: return null
        val allPasswords = breachesContent.details?.let {
            withContext(Dispatchers.Default) { passwordsAnalysis.extractPasswordMap(it) }
        }
        return breachesContent.lastUpdateDate to allBreaches.map { toBreachWithJson(it, allPasswords) }
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
        if (System.currentTimeMillis() - lastRefreshCache < DELAY_BEFORE_REFRESH) {
            
            return
        }

        val session = sessionManager.session ?: return
        val login = session.userId
        val remoteStatus = try {
            darkWebService.getEmailStatus(login, session.uki)
                .content
                ?: return
        } catch (e: Exception) {
            return
        }
        val cachedEmailsStatus = remoteStatus.emailsState?.mapNotNull {
            it.email?.let { email ->
                DarkWebEmailStatus(email, it.state ?: DarkWebEmailStatus.STATUS_DISABLED)
            }
        }
        cache = Cache(
            session.sessionId,
            cachedEmailsStatus,
            System.currentTimeMillis()
        )
    }

    private suspend fun toBreachWithJson(it: JsonObject, passwords: Map<String, List<String>?>?):
            BreachWithOriginalJson {
        return withContext(Dispatchers.Default) {
            val originalJson = it.toString()
            val breach = gson.fromJson(it, Breach::class.java)
            BreachWithOriginalJson(
                breach, originalJson,
                passwords = passwords?.get(breach.id)
            )
        }
    }

    

    private class Cache(
        val sessionId: String,
        val emailsStatus: List<DarkWebEmailStatus>?,
        val fetchTimestamp: Long
    )

    companion object {
        const val ERROR_UNKNOWN = "UNKNOWN"
        private const val DELAY_BEFORE_REFRESH = 2 * 60 * 1000 
    }
}