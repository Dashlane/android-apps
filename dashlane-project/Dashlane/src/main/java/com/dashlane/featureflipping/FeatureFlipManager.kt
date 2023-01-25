package com.dashlane.featureflipping

import com.dashlane.debug.DaDaDa
import com.dashlane.network.webservices.FeatureFlipService
import com.dashlane.session.BySessionRepository
import com.dashlane.session.RemoteConfiguration
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.useractivity.log.usage.UsageLogCode111
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.UserChangedDetector
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class FeatureFlipManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val service: FeatureFlipService,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val dadada: DaDaDa,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    @GlobalCoroutineScope private val coroutineScope: CoroutineScope
) : RemoteConfiguration {

    var featureFlips: JsonObject? = null
        get() {
            resetIfUserChanged()
            return field
        }
        private set

    private var currentJson
        get() = currentValue?.let { runCatching { parseJson(it) }.getOrNull() }
        set(value) {
            currentValue = value?.toString()
        }

    private var currentValue
        get() = sessionManager.session?.let(userSecureStorageManager::readUserFeature)
        set(value) {
            sessionManager.session?.let { userSecureStorageManager.storeUserFeature(it, value ?: "") }
        }

    private val userChangedDetector = UserChangedDetector(sessionManager)
    private var lastSuccessfulRefreshTime: Long = 0L

    init {
        coroutineScope.launch {
            dadada.dataFlow.collect {
                reset()
            }
        }
    }

    override fun launchRefreshIfNeeded() {
        if (shouldSkipRefresh()) {
            return
        }

        coroutineScope.launch {
            refreshIfNeeded()
        }
    }

    

    override suspend fun refreshIfNeeded() {
        if (shouldSkipRefresh()) {
            return
        }

        val session = sessionManager.session ?: return
        val login = session.userId
        val uki = session.uki
        val content = try {
            val features = UserFeaturesChecker.FeatureFlip.values().map { it.value }
            service.execute(login, uki, JSONArray(features).toString()).takeIf { it.isSuccess() }?.content ?: return
        } catch (t: Throwable) {
            return
        }

        setUserFeatureData(login, content)
    }

    private fun shouldSkipRefresh(): Boolean {
        resetIfUserChanged()
        return lastSuccessfulRefreshTime > 0 && System.currentTimeMillis() - lastSuccessfulRefreshTime < MIN_INTERVAL_REFRESH
    }

    fun reset() {
        lastSuccessfulRefreshTime = 0
        load()
    }

    private fun setUserFeatureData(login: String, json: JsonObject) {
        resetIfUserChanged()
        val currentLogin = userChangedDetector.lastUsername
        if (currentLogin == null || login != currentLogin) {
            return 
        }
        lastSuccessfulRefreshTime = System.currentTimeMillis()
        save(json)
        load(json)
    }

    private fun resetIfUserChanged() {
        if (!userChangedDetector.hasUserChanged()) {
            return 
        }
        reset()
    }

    private fun save(json: JsonObject) {
        val currentJson = this.currentJson
        if (json == currentJson) {
            
            return
        }
        logDifferences(currentJson ?: JsonObject(), json)
        this.currentJson = json
    }

    override fun load(): RemoteConfiguration.LoadResult {
        userChangedDetector.refresh()
        val login = userChangedDetector.lastUsername
        if (login != null) {
            load(currentJson)
        } else {
            load(null)
        }
        return if (featureFlips != null) RemoteConfiguration.LoadResult.Success else RemoteConfiguration.LoadResult.Failure
    }

    private fun load(json: JsonObject?) {
        val overriddenJson = dadada.featureFlippingJson?.let { parseJson(it) } ?: json ?: return
        featureFlips = overriddenJson
    }

    private fun logDifferences(currentData: JsonObject, newData: JsonObject) {
        val currentKeys = currentData.keySet()
        val allKeys = currentKeys + newData.keySet()

        for (key in allKeys) {
            val oldValue = currentData[key]?.asBoolean ?: false
            val newValue = newData[key]?.asBoolean ?: false

            if (oldValue != newValue) {
                trackValueChanged(key, newValue)
            }
        }
    }

    private fun trackValueChanged(key: String, value: Boolean) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode111(
                    type = "feature_$key",
                    status = if (value) "true" else "false"
                )
            )
    }

    companion object {
        private val MIN_INTERVAL_REFRESH = TimeUnit.MINUTES.toMillis(10)

        fun parseJson(json: String) =
            runCatching { JsonParser.parseString(json) as JsonObject }.getOrNull()
    }
}
