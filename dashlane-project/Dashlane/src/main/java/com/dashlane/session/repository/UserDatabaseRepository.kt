package com.dashlane.session.repository

import com.dashlane.login.LoginInfo
import com.dashlane.session.Session

interface UserDatabaseRepository {

    fun getRacletteDatabase(session: Session): RacletteDatabase?

    suspend fun sessionCleanup(session: Session, forceLogout: Boolean)

    suspend fun sessionInitializing(session: Session, loginInfo: LoginInfo)

    suspend fun openRacletteDatabase(session: Session)

    fun cleanupRacletteDatabase(session: Session)

    fun isRacletteDatabaseAccessible(session: Session): Boolean
}

typealias RacletteDatabase = com.dashlane.database.Database
