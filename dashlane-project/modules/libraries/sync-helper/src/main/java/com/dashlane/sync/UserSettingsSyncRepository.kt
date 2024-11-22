package com.dashlane.sync

import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.domain.Transaction
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

class UserSettingsSyncRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDataRepository
) {
    fun insertOrUpdateForSync(value: SyncObject.Settings, backupTimeMillis: Long) {
        val session = sessionManager.session!!
        userDataRepository.getSettingsManager(session).updateSettings(value)
        val preferencesForCurrentUser = preferencesManager[session.username]

        preferencesForCurrentUser.userSettingsShouldSync = false
        preferencesForCurrentUser.userSettingsBackupTimeMillis = backupTimeMillis
    }

    suspend fun getOutgoingTransactions(): List<OutgoingTransaction> {
        val session = sessionManager.session!!
        val preferencesForCurrentUser = preferencesManager[session.username]
        return if (preferencesForCurrentUser.userSettingsShouldSync) {
            fetchAsOutgoingUpdate()
        } else {
            emptyList()
        }
    }

    suspend fun fetchAsOutgoingUpdate(): List<OutgoingTransaction.Update> {
        val session = sessionManager.session!!
        val settings = userDataRepository.getSettingsManager(session).loadSettings()
        val pendingOperation = OutgoingTransaction.Update(
            Transaction(
                "SETTINGS_userId",
                Instant.ofEpochMilli(preferencesManager[session.username].userSettingsBackupTimeMillis)
            ),
            settings,
            settings
        )
        return listOf(pendingOperation)
    }

    suspend fun getSummary(): SyncSummaryItem {
        val session = sessionManager.session!!
        val settings = userDataRepository.getSettingsManager(session).loadSettings()
        val settingsBackupTime = preferencesManager[session.username].userSettingsBackupTime
        return SyncSummaryItem(
            objectId = settings.id ?: "SETTINGS_userId",
            lastUpdateTime = settingsBackupTime,
            syncObjectType = SyncObjectType.SETTINGS
        )
    }

    fun applyBackupDate(backupTime: Instant) {
        val session = sessionManager.session!!
        preferencesManager[session.username].userSettingsBackupTimeMillis = backupTime.toEpochMilli()
    }

    fun deleteForSync() {
        error("Shouldn't delete settings")
    }

    fun clearPendingOperations() {
        userDataRepository.getSettingsManager(sessionManager.session!!).shouldSyncSettings = false
    }
}