package com.dashlane.session.observer

import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.storage.securestorage.SecureStorageLocalKeyCryptographyMarkerMigration
import com.dashlane.usercryptography.UserCryptographyRepository
import javax.inject.Inject

class CryptographyMigrationObserver @Inject constructor(
    private val userCryptographyRepository: UserCryptographyRepository,
    private val localKeyCryptographyMarkerMigration: SecureStorageLocalKeyCryptographyMarkerMigration
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        migrateLocalKeyIfNeeded(session)
    }

    private fun migrateLocalKeyIfNeeded(session: Session) {
        
        val cryptographyMarker = userCryptographyRepository.getCryptographyMarker(session) ?: return

        localKeyCryptographyMarkerMigration.migrateLocalKeyIfNeeded(session.appKey, session.username, cryptographyMarker)
    }
}