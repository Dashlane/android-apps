package com.dashlane.session.repository

import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.user.UserAccountInfo
import com.dashlane.xml.domain.SyncObject

interface UserDataRepository : BySessionRepository<SettingsManager> {
    fun getSettingsManager(session: Session): SettingsManager

    suspend fun sessionCleanup(session: Session, forceLogout: Boolean)

    suspend fun sessionInitializing(
        session: Session,
        userSettings: SyncObject.Settings?,
        accountType: UserAccountInfo.AccountType,
        allowOverwriteAccessKey: Boolean
    )
}