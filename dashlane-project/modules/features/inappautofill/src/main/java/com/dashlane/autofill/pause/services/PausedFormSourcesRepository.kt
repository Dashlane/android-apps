package com.dashlane.autofill.pause.services

import java.time.Instant

interface PausedFormSourcesRepository {
    suspend fun isApplicationPaused(formSourceIdentifier: String): Boolean
    suspend fun isWebsitePaused(formSourceIdentifier: String): Boolean

    suspend fun pauseApplication(formSourceIdentifier: String, untilInstant: Instant)
    suspend fun pauseWebsite(formSourceIdentifier: String, untilInstant: Instant)

    suspend fun resumeApplication(formSourceIdentifier: String)
    suspend fun resumeWebsite(formSourceIdentifier: String)

    suspend fun resumeAll()

    suspend fun allPausedApplications(): Map<String, Instant>
    suspend fun allPausedWebsites(): Map<String, Instant>
}