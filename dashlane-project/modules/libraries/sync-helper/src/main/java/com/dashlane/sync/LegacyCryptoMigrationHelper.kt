package com.dashlane.sync

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.CryptoAlgorithm
import com.dashlane.hermes.generated.definitions.CryptoMigrationStatus
import com.dashlane.hermes.generated.definitions.CryptoMigrationType
import com.dashlane.hermes.generated.events.user.MigrateCrypto
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.Session
import com.dashlane.session.authorization
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.sync.cryptochanger.SyncCryptoChanger
import com.dashlane.sync.cryptochanger.SyncCryptoChangerCryptographyException
import com.dashlane.sync.cryptochanger.SyncCryptoChangerDownloadException
import com.dashlane.sync.cryptochanger.SyncCryptoChangerUploadException
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class LegacyCryptoMigrationHelper @Inject constructor(
    private val syncCryptoChanger: SyncCryptoChanger,
    private val userDataRepository: UserDataRepository,
    private val preferencesManager: PreferencesManager,
    private val logRepository: LogRepository
) {
    suspend fun migrateCryptoIfNeeded(session: Session) {
        val settingsManager = userDataRepository.getSettingsManager(session)
        val userPreferencesManager = preferencesManager[session.username]
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
                authorization = session.authorization,
                appKey = session.appKey,
                vaultKey = session.vaultKey,
                cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d 
            )
            CryptoMigrationStatus.SUCCESS
        } catch (e: SyncCryptoChangerCryptographyException) {
            CryptoMigrationStatus.ERROR_REENCRYPTION
        } catch (e: SyncCryptoChangerDownloadException) {
            CryptoMigrationStatus.ERROR_DOWNLOAD
        } catch (e: SyncCryptoChangerUploadException) {
            CryptoMigrationStatus.ERROR_UPLOAD
        } catch (t: Throwable) {
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

    companion object {
        private const val CRYPTO_MIGRATION_PERIOD_DAYS = 1L
    }
}