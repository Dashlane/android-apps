package com.dashlane.session.observer

import android.app.backup.BackupManager
import com.dashlane.account.UserAccountStorage
import com.dashlane.cryptography.encryptUtf8ToBase64String
import com.dashlane.login.LoginInfo
import com.dashlane.network.tools.authorization
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceTokenGeneratorService
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.session.repository.UserCryptographyRepository
import java.time.Instant
import java.time.temporal.ChronoUnit



class BackupTokenObserver(
    private val api: DashlaneApi,
    private val userAccountStorage: UserAccountStorage,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val backupManager: BackupManager,
    private val userCryptographyRepository: UserCryptographyRepository
) : SessionObserver {

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        val username = session.username
        val securitySettings = userAccountStorage[username]?.securitySettings
        if (securitySettings?.isToken == false) {
            if (globalPreferencesManager.getCipheredBackupToken(username) != null) {
                
                globalPreferencesManager.deleteBackupToken(username)
                backupManager.dataChanged()
            }
            return
        }
        val nextSyncTime = globalPreferencesManager.getBackupTokenDate(username).plus(7, ChronoUnit.DAYS)
        if (nextSyncTime.isBefore(Instant.now())) {
            
            runCatching {
                val result = api.endpoints.authentication.authRegistrationExtraDeviceTokenGeneratorService
                    .execute(
                        session.authorization,
                        AuthRegistrationExtraDeviceTokenGeneratorService.Request(
                            AuthRegistrationExtraDeviceTokenGeneratorService.Request.TokenType.GOOGLEACCOUNTNEWDEVICE
                        )
                    )
                val cryptographyEngineFactory = userCryptographyRepository.getCryptographyEngineFactory(session)
                val cipheredToken = cryptographyEngineFactory.createEncryptionEngine().use { encryptionEngine ->
                    encryptionEngine.encryptUtf8ToBase64String(result.data.token, compressed = true)
                }
                globalPreferencesManager.setCipheredBackupToken(username, cipheredToken.value)
                backupManager.dataChanged()
            }
        }
    }
}