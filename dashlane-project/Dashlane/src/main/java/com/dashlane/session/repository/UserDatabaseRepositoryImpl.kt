package com.dashlane.session.repository

import com.dashlane.database.DatabaseProvider
import com.dashlane.login.LoginInfo
import com.dashlane.session.LocalKey
import com.dashlane.session.Session
import com.dashlane.useractivity.RacletteLogger
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UserDatabaseRepositoryImpl @Inject constructor(
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val databaseProvider: DatabaseProvider,
    private val racletteLogger: RacletteLogger
) : UserDatabaseRepository {
    override fun getRacletteDatabase(session: Session): RacletteDatabase? =
        databaseProvider.getDatabase(session.userId)

    override suspend fun sessionInitializing(session: Session, loginInfo: LoginInfo) {
        sessionCoroutineScopeRepository.getCoroutineScope(session)?.let {
            withContext(it.coroutineContext) {
                openRacletteDatabase(session)
            }
        }
    }

    override suspend fun sessionCleanup(session: Session, forceLogout: Boolean) {
        cleanupRacletteDatabase(session)
    }

    override suspend fun openRacletteDatabase(session: Session) {
        runCatching {
            session.localKey.use(LocalKey::cryptographyKey).use { key ->
                databaseProvider.open(session.userId, key)
            }
        }.onFailure {
            racletteLogger.exceptionOpenDatabase(it)
        }.getOrThrow()
    }

    override fun cleanupRacletteDatabase(session: Session) {
        databaseProvider.closeDatabase(session.userId)
    }

    override fun isRacletteDatabaseAccessible(session: Session): Boolean =
        databaseProvider.isAccessible(session.userId, session.localKey.cryptographyKey)
}