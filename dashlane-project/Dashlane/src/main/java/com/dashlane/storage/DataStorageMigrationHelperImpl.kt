package com.dashlane.storage

import android.content.Context
import com.dashlane.CipherDatabaseUtils
import com.dashlane.core.DataSync
import com.dashlane.database.DatabaseProvider
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorageMigrationHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val provider: DatabaseProvider,
    private val userDatabaseRepository: UserDatabaseRepository,
    private val racletteLogger: RacletteLogger,
    private val mainDataAccessor: MainDataAccessor,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val dataSync: DataSync,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) : DataStorageMigrationHelper {

    private val enableLocalMigrationIfRemoteFails: Boolean
        get() = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.DATABASE_LOCAL_MIGRATION)

    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

    private val session: Session?
        get() = sessionManager.session

    private val useLegacyDatabase: Boolean
        get() = userDatabaseRepository.getDatabase(session!!) != null

    private var job: Job? = null

    override fun logSyncFailIfLegacyDatabase(e: Throwable, lastSyncTime: Instant) {
        if (useLegacyDatabase) {
            racletteLogger.syncFailOnLegacyDatabase(e, lastSyncTime)
        }
    }

    override fun logShouldStartMigrationPreSync() {
        if (useLegacyDatabase) {
            racletteLogger.shouldStartMigrationPreSync()
        }
    }

    override fun logShouldStartMigrationPreCrypto() {
        if (useLegacyDatabase) {
            racletteLogger.shouldStartMigrationPreCrypto()
        }
    }

    override suspend fun waiting() {
        job?.join()
    }

    override fun migration(coroutineScope: CoroutineScope, action: suspend () -> Unit) {
        if (!useLegacyDatabase) return

        job = coroutineScope.launch {
            val session = session ?: return@launch
            val username = session.userId
            racletteLogger.migrationStart()
            runCatching { prepareMigration(session) }
                .onFailure {
                    racletteLogger.exception(it)
                    racletteLogger.migrationFailure(it)
                }
                .getOrThrow()

            runCatching {
                action.invoke()
            }.onSuccess {
                deleteLegacyDatabase(username)
                userDatabaseRepository.cleanupLegacyDatabase(session)
                racletteLogger.migrationSuccess()
                racletteLogger.openDatabase()
            }.onFailure {
                deleteRacletteDatabase(username)
                userDatabaseRepository.cleanupRacletteDatabase(session)
                racletteLogger.exception(it)
                racletteLogger.migrationFailure(it)
            }.getOrThrow()
        }
    }

    

    private suspend fun prepareMigration(session: Session) {
        provider.delete(session.userId)
        userDatabaseRepository.openRacletteDatabase(session)
    }

    private suspend fun deleteLegacyDatabase(username: String) =
        withContext(ioCoroutineDispatcher) {
            context.deleteDatabase(CipherDatabaseUtils.getDatabaseName(username))
        }

    private suspend fun deleteRacletteDatabase(username: String) = provider.delete(username)

    override suspend fun localMigration() {
        if (!useLegacyDatabase) return
        if (!enableLocalMigrationIfRemoteFails) return

        withContext(ioCoroutineDispatcher) {
            val session = session ?: return@withContext
            val username = session.userId
            runCatching { prepareMigration(session) }
                .onFailure {
                    racletteLogger.exception(it)
                    racletteLogger.migrationFailure(it)
                }
                .getOrThrow()

            runCatching {
                val items = vaultDataQuery.queryAll(vaultFilter {
                    allStatusFilter()
                    ignoreUserLock()
                })
                val database = userDatabaseRepository.getRacletteDatabase(session)
                    ?: return@withContext
                database.vaultObjectRepository.transaction {
                    items.forEach { update(it) }
                }
            }.onSuccess {
                deleteLegacyDatabase(username)
                userDatabaseRepository.cleanupLegacyDatabase(session)
                racletteLogger.localMigrationSuccess()
                racletteLogger.openDatabase()
                dataSync.sync()
            }.onFailure {
                deleteRacletteDatabase(username)
                userDatabaseRepository.cleanupRacletteDatabase(session)
                racletteLogger.exception(it)
                racletteLogger.localMigrationFailure(it)
            }.getOrThrow()
        }
    }
}
