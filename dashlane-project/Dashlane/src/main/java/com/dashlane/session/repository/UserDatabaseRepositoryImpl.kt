package com.dashlane.session.repository

import android.content.Context
import com.dashlane.CipherDatabaseUtils
import com.dashlane.database.DatabaseProvider
import com.dashlane.db.UpdateManager
import com.dashlane.login.LoginInfo
import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.performancelogger.TimeToRacletteOpenLogger
import com.dashlane.session.LocalKey
import com.dashlane.session.Session
import com.dashlane.storage.userdata.CipherDatabase
import com.dashlane.storage.userdata.Database
import com.dashlane.storage.userdata.accessor.DataCounterImpl
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.debug.DaDaDa
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
open class UserDatabaseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val notificationBadgeActor: Lazy<NotificationBadgeActor>,
    private val dataCounter: Lazy<DataCounterImpl>,
    private val installLogRepository: InstallLogRepository,
    private val databaseProvider: DatabaseProvider,
    private val racletteLogger: RacletteLogger,
    private val timeToRacletteOpenLogger: TimeToRacletteOpenLogger,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val daDaDa: DaDaDa
) : UserDatabaseRepository {

    private val enableRacletteNewDevice: Boolean
        get() = !daDaDa.createLegacyDatabaseNewDevice()

    private val dropLegacyDatabase: Boolean
        get() = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.DROP_LEGACY_DATABASE)

    private val databasePerSession = mutableMapOf<Session, Database>()

    override fun getRacletteDatabase(session: Session): RacletteDatabase? =
        databaseProvider.getDatabase(session.userId)

    override fun getDatabase(session: Session) = databasePerSession[session]

    

    override suspend fun sessionInitializing(session: Session, loginInfo: LoginInfo) {
        withContext(sessionCoroutineScopeRepository.getCoroutineScope(session).coroutineContext) {
            when {
                
                loginInfo.isFirstLogin && enableRacletteNewDevice -> openRacletteDatabase(session)

                
                
                loginInfo.isFirstLogin && !enableRacletteNewDevice -> openLegacyDatabase(session)

                
                !session.isLegacyDatabaseExist(context) -> openRacletteDatabase(session)

                
                dropLegacyDatabase -> {
                    context.deleteDatabase(CipherDatabaseUtils.getDatabaseName(session.userId))
                    openRacletteDatabase(session)
                    racletteLogger.dropLegacyDatabase()
                }
                
                else -> {
                    openLegacyDatabase(session)
                    racletteLogger.notDropLegacyDatabase()
                }
            }
            racletteLogger.openDatabase()
        }
    }

    override suspend fun sessionCleanup(session: Session, forceLogout: Boolean) {
        cleanupLegacyDatabase(session)
        cleanupRacletteDatabase(session)
    }

    override suspend fun openRacletteDatabase(session: Session) {
        timeToRacletteOpenLogger.logStart()
        runCatching {
            session.localKey.use(LocalKey::cryptographyKey).use { key ->
                databaseProvider.open(session.userId, key)
            }
        }.onSuccess {
            logStopRacletteOpen(session)
        }.onFailure {
            racletteLogger.exceptionOpenDatabase(it)
            timeToRacletteOpenLogger.clear()
        }.getOrThrow()
    }

    override fun cleanupRacletteDatabase(session: Session) {
        databaseProvider.closeDatabase(session.userId)
    }

    override fun cleanupLegacyDatabase(session: Session) {
        getDatabase(session)?.lockDatabase()
        databasePerSession.remove(session)
    }

    override fun isRacletteDatabaseAccessible(session: Session): Boolean =
        databaseProvider.isAccessible(session.userId, session.localKey.cryptographyKey)

    private fun openLegacyDatabase(session: Session) {
        
        val database = initialize(context, session.userId)
        database.unlockDatabase(session)
        databasePerSession[session] = database
    }

    

    private fun initialize(context: Context, login: String): Database {
        val database = CipherDatabase(context, login, UpdateManager(racletteLogger), installLogRepository)
        database.addListener(notificationBadgeActor.get())
        val dataCount = dataCounter.get()
        if (dataCount is Database.OnUpdateListener) {
            database.addListener(dataCount as Database.OnUpdateListener)
        }
        return database
    }

    

    private fun Database.unlockDatabase(session: Session) {
        open(session.appKey, session.localKey)
    }

    

    private fun Database.lockDatabase() {
        if (inTransaction()) {
            endTransaction()
        }
        close()
    }

    private fun logStopRacletteOpen(session: Session) {
        
        if (getRacletteDatabase(session)?.memorySummaryRepository?.databaseSyncSummary?.items?.size != 0) {
            timeToRacletteOpenLogger.logStop()
        } else {
            timeToRacletteOpenLogger.clear()
        }
    }
}