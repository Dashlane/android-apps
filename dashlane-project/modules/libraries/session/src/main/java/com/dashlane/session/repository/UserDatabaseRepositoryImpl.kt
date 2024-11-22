package com.dashlane.session.repository

import com.dashlane.crypto.keys.LocalKey
import com.dashlane.database.DatabaseProvider
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UserDatabaseRepositoryImpl @Inject constructor(
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val databaseProvider: DatabaseProvider
) : UserDatabaseRepository {
    override fun getRacletteDatabase(session: Session): RacletteDatabase? =
        databaseProvider.getDatabase(session.userId)

    override suspend fun sessionInitializing(session: Session, loginInfo: LoginInfo) {
        sessionCoroutineScopeRepository.getCoroutineScope(session)?.let {
            openRacletteDatabase(session)
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
        }.getOrThrow()
    }

    override fun cleanupRacletteDatabase(session: Session) {
        databaseProvider.closeDatabase(session.userId)
    }

    override fun isRacletteDatabaseAccessibleLegacy(session: Session): Boolean = runBlocking {
        isRacletteDatabaseAccessible(session)
    }

    override suspend fun isRacletteDatabaseAccessible(session: Session): Boolean {
        return databaseProvider.isAccessible(session.userId, session.localKey.cryptographyKey)
    }
}