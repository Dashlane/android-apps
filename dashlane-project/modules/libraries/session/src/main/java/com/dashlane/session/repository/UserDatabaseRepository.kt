package com.dashlane.session.repository

import androidx.annotation.Discouraged
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session

typealias RacletteDatabase = com.dashlane.database.Database

interface UserDatabaseRepository {

    fun getRacletteDatabase(session: Session): RacletteDatabase?

    suspend fun sessionCleanup(session: Session, forceLogout: Boolean)

    suspend fun sessionInitializing(session: Session, loginInfo: LoginInfo)

    suspend fun openRacletteDatabase(session: Session)

    fun cleanupRacletteDatabase(session: Session)

    @Discouraged("Use the suspend version instead")
    fun isRacletteDatabaseAccessibleLegacy(session: Session): Boolean

    suspend fun isRacletteDatabaseAccessible(session: Session): Boolean
}