package com.dashlane.featureflipping

import com.dashlane.debug.DaDaDa
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.features.FeatureFlipGetAndEvaluateForUserService
import com.dashlane.session.RemoteConfiguration
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.util.UserChangedDetector
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.isSemanticallyNull
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlipManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val service: FeatureFlipGetAndEvaluateForUserService,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val dadada: DaDaDa,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope
) : RemoteConfiguration {

    var featureFlips: List<String>? = null
        get() {
            resetIfUserChanged()
            return field
        }
        private set

    private var currentValue: String?
        get() = sessionManager.session?.let(userSecureStorageManager::readUserFeature)
        set(value) {
            sessionManager.session?.let {
                userSecureStorageManager.storeUserFeature(it, value ?: "")
            }
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
        val request = FeatureFlipGetAndEvaluateForUserService.Request(
            FeatureFlip.values().map { it.value }
        )
        runCatching {
            val enabledFeatures = service.execute(
                session.authorization,
                request
            ).data.enabledFeatures.toFeatureFlipString()

            setUserFeatureData(login, enabledFeatures)
        }
    }

    private fun shouldSkipRefresh(): Boolean {
        resetIfUserChanged()
        return lastSuccessfulRefreshTime > 0 && System.currentTimeMillis() - lastSuccessfulRefreshTime < MIN_INTERVAL_REFRESH
    }

    fun reset() {
        lastSuccessfulRefreshTime = 0
        load()
    }

    private fun setUserFeatureData(login: String, json: String) {
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

    private fun save(json: String) {
        if (json == currentValue) {
            
            return
        }
        this.currentValue = json
    }

    override fun load(): RemoteConfiguration.LoadResult {
        userChangedDetector.refresh()
        val login = userChangedDetector.lastUsername
        if (login != null) {
            load(currentValue)
        } else {
            load(null)
        }
        return if (featureFlips != null) RemoteConfiguration.LoadResult.Success else RemoteConfiguration.LoadResult.Failure
    }

    private fun load(json: String?) {
        val overriddenJson = dadada.featureFlippingString ?: json?.toFeatureFlips() ?: return
        featureFlips = overriddenJson
    }

    companion object {
        private val MIN_INTERVAL_REFRESH = TimeUnit.MINUTES.toMillis(10)
        private val gson = Gson()
        fun String.toFeatureFlips(): List<String> =
            if (isSemanticallyNull()) {
                emptyList()
            } else {
                gson.fromJson(this, Array<String>::class.java).toList()
            }

        fun List<String>.toFeatureFlipString(): String = gson.toJson(this)
    }
}
