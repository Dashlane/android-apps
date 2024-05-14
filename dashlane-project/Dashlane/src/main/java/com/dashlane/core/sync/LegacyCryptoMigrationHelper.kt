package com.dashlane.core.sync

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CryptoAlgorithm
import com.dashlane.hermes.generated.definitions.CryptoMigrationStatus
import com.dashlane.hermes.generated.definitions.CryptoMigrationType
import com.dashlane.hermes.generated.events.user.MigrateCrypto
import com.dashlane.common.logger.developerinfo.DeveloperInfoLogger
import com.dashlane.network.tools.authorization
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.UserDataRepository
import com.dashlane.sync.cryptochanger.SyncCryptoChanger
import com.dashlane.sync.cryptochanger.SyncCryptoChangerCryptographyException
import com.dashlane.sync.cryptochanger.SyncCryptoChangerDownloadException
import com.dashlane.sync.cryptochanger.SyncCryptoChangerUploadException
import com.dashlane.util.stackTraceToSafeString
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class LegacyCryptoMigrationHelper @Inject constructor(
    private val syncCryptoChanger: SyncCryptoChanger,
    private val userDataRepository: UserDataRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val logRepository: LogRepository,
    private val developerInfoLogger: DeveloperInfoLogger
) {
    suspend fun migrateCryptoIfNeeded(session: Session) {
        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings()
        val cryptoUserPayload = settings.cryptoUserPayload
        if (cryptoUserPayload.isNullOrEmpty() || cryptoUserPayload.toCryptographyMarkerOrNull() == CryptographyMarker.Kwc3) {
            
            val cryptoMigrationAttemptDate = userPreferencesManager.cryptoMigrationAttemptDate
            val now = Instant.now()
            val shouldAttemptCryptoMigration =
                cryptoMigrationAttemptDate == null ||
                    cryptoMigrationAttemptDate.plus(CRYPTO_MIGRATION_PERIOD_DAYS, ChronoUnit.DAYS) < now
            if (shouldAttemptCryptoMigration) {
                userPreferencesManager.cryptoMigrationAttemptDate = now

                
                val cryptoMigrationStatus = migrateCrypto(session)
                logCryptoMigrationResult(cryptoMigrationStatus)
            }
        }
    }

    private suspend fun migrateCrypto(session: Session): CryptoMigrationStatus =
        try {
            syncCryptoChanger.updateCryptography(
                session.authorization,
                session.userKeys,
                CryptographyMarker.Flexible.Defaults.argon2d 
            )
            CryptoMigrationStatus.SUCCESS
        } catch (e: SyncCryptoChangerCryptographyException) {
            logException(CryptoMigrationStatus.ERROR_REENCRYPTION.code, e)
            CryptoMigrationStatus.ERROR_REENCRYPTION
        } catch (e: SyncCryptoChangerDownloadException) {
            logException(CryptoMigrationStatus.ERROR_DOWNLOAD.code, e)
            CryptoMigrationStatus.ERROR_DOWNLOAD
        } catch (e: SyncCryptoChangerUploadException) {
            logException(CryptoMigrationStatus.ERROR_UPLOAD.code, e)
            CryptoMigrationStatus.ERROR_UPLOAD
        } catch (t: Throwable) {
            logException("error_unexpected", t)
            throw t
        }

    private fun logCryptoMigrationResult(status: CryptoMigrationStatus) {
        logRepository.queueEvent(
            MigrateCrypto(
                previousCrypto = CryptoAlgorithm.KWC3,
                newCrypto = CryptoAlgorithm.ARGON2D,
                type = CryptoMigrationType.MIGRATE_LEGACY,
                status = status
            )
        )
    }

    private fun logException(error: String, t: Throwable) {
        developerInfoLogger.log(
            action = "migrate_legacy_crypto_error",
            message = t.stackTraceToSafeString(),
            exceptionType = error
        )
    }

    companion object {
        private const val CRYPTO_MIGRATION_PERIOD_DAYS = 1L
    }
}