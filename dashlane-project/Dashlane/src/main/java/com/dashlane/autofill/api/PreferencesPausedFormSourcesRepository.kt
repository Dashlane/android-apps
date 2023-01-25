package com.dashlane.autofill.api

import androidx.annotation.VisibleForTesting
import com.dashlane.autofill.api.pause.services.PausedFormSourcesRepository
import com.dashlane.preference.ConstantsPrefs.Companion.PAUSED_APP_SOURCES_LIST
import com.dashlane.preference.ConstantsPrefs.Companion.PAUSED_WEB_SOURCES_LIST
import com.dashlane.preference.GlobalPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext



class PreferencesPausedFormSourcesRepository @VisibleForTesting constructor(
    val globalPreferencesManager: GlobalPreferencesManager,
    val coroutineContext: CoroutineContext = Dispatchers.IO
) : PausedFormSourcesRepository {

    @Inject
    constructor(globalPreferencesManager: GlobalPreferencesManager) : this(globalPreferencesManager, Dispatchers.IO)

    private val applicationFormSources =
        globalPreferencesManager.getStringSet(PAUSED_APP_SOURCES_LIST)?.toMap() ?: mutableMapOf()
    private val websitesFormSources =
        globalPreferencesManager.getStringSet(PAUSED_WEB_SOURCES_LIST)?.toMap() ?: mutableMapOf()

    override suspend fun isApplicationPaused(formSourceIdentifier: String): Boolean {
        return withContext(coroutineContext) {
            isFormSourcePaused(applicationFormSources, formSourceIdentifier)
        }
    }

    override suspend fun isWebsitePaused(formSourceIdentifier: String): Boolean {
        return withContext(coroutineContext) {
            isFormSourcePaused(websitesFormSources, formSourceIdentifier)
        }
    }

    override suspend fun pauseApplication(formSourceIdentifier: String, untilInstant: Instant) {
        withContext(coroutineContext) {
            addPauseToPreferences(PAUSED_APP_SOURCES_LIST, formSourceIdentifier, applicationFormSources, untilInstant)
        }
    }

    override suspend fun pauseWebsite(formSourceIdentifier: String, untilInstant: Instant) {
        withContext(coroutineContext) {
            addPauseToPreferences(PAUSED_WEB_SOURCES_LIST, formSourceIdentifier, websitesFormSources, untilInstant)
        }
    }

    override suspend fun resumeApplication(formSourceIdentifier: String) {
        withContext(coroutineContext) {
            removePauseFromPreferences(PAUSED_APP_SOURCES_LIST, formSourceIdentifier, applicationFormSources)
        }
    }

    override suspend fun resumeWebsite(formSourceIdentifier: String) {
        withContext(coroutineContext) {
            removePauseFromPreferences(PAUSED_WEB_SOURCES_LIST, formSourceIdentifier, websitesFormSources)
        }
    }

    override suspend fun resumeAll() {
        withContext(coroutineContext) {
            globalPreferencesManager.remove(PAUSED_APP_SOURCES_LIST)
            globalPreferencesManager.remove(PAUSED_WEB_SOURCES_LIST)
            applicationFormSources.clear()
            websitesFormSources.clear()
        }
    }

    override suspend fun allPausedApplications(): Map<String, Instant> {
        return withContext(coroutineContext) {
            applicationFormSources.toMap()
        }
    }

    override suspend fun allPausedWebsites(): Map<String, Instant> {
        return withContext(coroutineContext) {
            websitesFormSources.toMap()
        }
    }

    private fun isFormSourcePaused(pausedFormSources: Map<String, Instant>, formSourceId: String): Boolean {
        return pausedFormSources[formSourceId]?.isAfter(Instant.now()) ?: false
    }

    private fun addPauseToPreferences(
        key: String,
        formSourceId: String,
        formSources: MutableMap<String, Instant>,
        pauseUntil: Instant
    ) {
        formSources[formSourceId] = pauseUntil
        globalPreferencesManager.remove(key)
        globalPreferencesManager.putStringSet(key, formSources.fromMap())
    }

    private fun removePauseFromPreferences(
        key: String,
        formSourceId: String,
        formSources: MutableMap<String, Instant>
    ) {
        formSources.remove(formSourceId)
        globalPreferencesManager.remove(key)
        globalPreferencesManager.putStringSet(key, formSources.fromMap())
    }

    private fun Set<String>.toMap(): MutableMap<String, Instant> {
        return this.map {
            it.toEntry()
        }.toMap().toMutableMap()
    }

    private fun String.toEntry(): Pair<String, Instant> {
        val pausedFromSource = this.split("#")
        return pausedFromSource[0] to Instant.parse(pausedFromSource[1])
    }

    private fun Map<String, Instant>.fromMap(): Set<String> {
        return this.map {
            it.fromEntry()
        }.toSet()
    }

    private fun Map.Entry<String, Instant>.fromEntry(): String {
        return "${this.key}#${this.value}"
    }
}
