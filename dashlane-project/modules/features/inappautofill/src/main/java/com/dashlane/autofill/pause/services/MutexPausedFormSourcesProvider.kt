package com.dashlane.autofill.pause.services

import com.dashlane.autofill.pause.model.PausedFormSource
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MutexPausedFormSourcesProvider @Inject constructor(
    private val pausedFormSourcesRepository: PausedFormSourcesRepository
) : PausedFormSourcesProvider {
    private val lock = Mutex()

    override suspend fun isPaused(autoFillFormSource: AutoFillFormSource): Boolean {
        return lock.withLock {
            when (autoFillFormSource) {
                is ApplicationFormSource -> pausedFormSourcesRepository.isApplicationPaused(autoFillFormSource.packageName)
                is WebDomainFormSource -> pausedFormSourcesRepository.isWebsitePaused(autoFillFormSource.webDomain)
            }
        }
    }

    override suspend fun pauseUntil(autoFillFormSource: AutoFillFormSource, untilInstant: Instant) {
        lock.withLock {
            when (autoFillFormSource) {
                is ApplicationFormSource -> pausedFormSourcesRepository.pauseApplication(
                    autoFillFormSource.packageName,
                    untilInstant
                )
                is WebDomainFormSource -> pausedFormSourcesRepository.pauseWebsite(
                    autoFillFormSource.webDomain,
                    untilInstant
                )
            }
        }
    }

    override suspend fun removePause(autoFillFormSource: AutoFillFormSource) {
        lock.withLock {
            when (autoFillFormSource) {
                is ApplicationFormSource -> pausedFormSourcesRepository.resumeApplication(autoFillFormSource.packageName)
                is WebDomainFormSource -> pausedFormSourcesRepository.resumeWebsite(autoFillFormSource.webDomain)
            }
        }
    }

    override suspend fun removeAllPauses() {
        lock.withLock {
            pausedFormSourcesRepository.resumeAll()
        }
    }

    override suspend fun getAllPausedFormSources(): List<PausedFormSource> {
        val now = Instant.now()
        return lock.withLock {
            val app = pausedFormSourcesRepository.allPausedApplications()
                    .filter {
                        it.value.isAfter(now)
                    }
                    .map {
                        it.key to it.value
                    }
                    .sortedBy {
                        it.first
                    }
                    .map {
                        PausedFormSource(
                            ApplicationFormSource(it.first),
                            it.second
                        )
                    }

            val web = pausedFormSourcesRepository.allPausedWebsites()
                .filter {
                    it.value.isAfter(now)
                }
                .map {
                    it.key to it.value
                }
                .sortedBy {
                    it.first
                }
                .map {
                    PausedFormSource(
                        WebDomainFormSource(
                            "",
                            it.first
                        ),
                        it.second
                    )
                }
            app + web
        }
    }

    override suspend fun getPausedFormSource(autoFillFormSource: AutoFillFormSource): PausedFormSource? {
        return when (autoFillFormSource) {
            is ApplicationFormSource -> getPausedApplication(autoFillFormSource)
            is WebDomainFormSource -> getPausedWebDomain(autoFillFormSource)
        }
    }

    private suspend fun getPausedApplication(applicationFormSource: ApplicationFormSource): PausedFormSource? {
        val now = Instant.now()
        return lock.withLock {
            pausedFormSourcesRepository.allPausedApplications()
                .filter {
                    it.key == applicationFormSource.packageName && it.value.isAfter(now)
                }
                .map {
                    PausedFormSource(ApplicationFormSource(it.key), it.value)
                }.firstOrNull()
        }
    }

    private suspend fun getPausedWebDomain(webDomainFormSource: WebDomainFormSource): PausedFormSource? {
        val now = Instant.now()
        return lock.withLock {
            pausedFormSourcesRepository.allPausedWebsites()
                .filter {
                    it.key == webDomainFormSource.webDomain && it.value.isAfter(now)
                }
                .map {
                    PausedFormSource(WebDomainFormSource("", it.key), it.value)
                }.firstOrNull()
        }
    }
}
