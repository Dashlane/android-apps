package com.dashlane.session.repository

import android.content.Context
import com.dashlane.CipherDatabaseUtils
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.storage.userdata.Database



interface UserDatabaseRepository {

    

    fun getRacletteDatabase(session: Session): RacletteDatabase?

    

    fun getDatabase(session: Session): Database?

    suspend fun sessionCleanup(session: Session, forceLogout: Boolean)

    suspend fun sessionInitializing(session: Session, loginInfo: LoginInfo)

    suspend fun openRacletteDatabase(session: Session)

    fun cleanupRacletteDatabase(session: Session)

    fun cleanupLegacyDatabase(session: Session)

    fun isRacletteDatabaseAccessible(session: Session): Boolean
}

typealias RacletteDatabase = com.dashlane.database.Database

fun Session.isLegacyDatabaseExist(context: Context) =
    runCatching { context.getDatabasePath(CipherDatabaseUtils.getDatabaseName(userId)).exists() }
        .getOrNull() ?: false
