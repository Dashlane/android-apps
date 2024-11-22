package com.dashlane.session.repository

import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.xml.domain.SyncObject.Settings
import com.dashlane.xml.domain.toTransaction
import com.dashlane.xml.serializer.XmlSerialization
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsManager(
    private val userSecureStorageManager: UserSecureStorageManager,
    private val preferencesManager: PreferencesManager,
    private val session: Session
) {

    var shouldSyncSettings: Boolean
        get() = preferencesManager[session.username].userSettingsShouldSync
        set(value) {
            preferencesManager[session.username].userSettingsShouldSync = value
        }

    private val settingsCacheLock = Mutex()

    private var settingsCache: Deferred<Settings>? = null

    private val validSettingsCache: Deferred<Settings>?
        get() = settingsCache?.takeUnless { it.isCompleted && it.isCancelled }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val loadDispatcher: CoroutineDispatcher = newSingleThreadContext(javaClass.simpleName)

    fun getSettings(): Settings =
        runCatching { runBlocking { loadSettings() } }.getOrDefault(Settings())

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun loadSettings(): Settings {
        val settingsCache = validSettingsCache
            ?: settingsCacheLock.withLock {
                validSettingsCache ?: GlobalScope.async(loadDispatcher) {
                    val settingsXml =
                        checkNotNull(userSecureStorageManager.readSettings(session.localKey, session.username)) {
                            "Unable to read settings"
                        }
                    deserializeSettings(settingsXml)
                }.also { settingsCache = it }
            }
        return settingsCache.await()
    }

    private fun deserializeSettings(settingsXml: String): Settings {
        val transactionXml = XmlSerialization.deserializeTransaction(settingsXml)
        return Settings(transactionXml.data)
    }

    suspend fun unloadSettings() {
        settingsCacheLock.withLock {
            settingsCache = null
        }
    }

    @JvmOverloads
    fun updateSettings(settings: Settings, triggerSync: Boolean = true) {
        val settingsXml = XmlSerialization.serializeTransaction(settings.toTransaction())
        settingsCache = CompletableDeferred(settings) 
        userSecureStorageManager.storeSettings(session.localKey, session.username, settingsXml)
        shouldSyncSettings = shouldSyncSettings || triggerSync
    }
}
