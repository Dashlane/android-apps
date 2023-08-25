package com.dashlane.core.sync

import com.dashlane.preference.UserPreferencesManager
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
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDataRepository
) {
    fun insertOrUpdateForSync(value: SyncObject.Settings, backupTimeMillis: Long) {
        val session = sessionManager.session!!
        userDataRepository.getSettingsManager(session).updateSettings(value)
        val preferencesForCurrentUser = userPreferencesManager.preferencesForCurrentUser()!!

        preferencesForCurrentUser.userSettingsShouldSync = false
        preferencesForCurrentUser.userSettingsBackupTimeMillis = backupTimeMillis
    }

    suspend fun getOutgoingTransactions(): List<OutgoingTransaction> {
        return if (userPreferencesManager.userSettingsShouldSync) {
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
                Instant.ofEpochMilli(userPreferencesManager.userSettingsBackupTimeMillis)
            ),
            settings,
            settings
        )
        return listOf(pendingOperation)
    }

    suspend fun getSummary(): SyncSummaryItem {
        val session = sessionManager.session!!
        val settings = userDataRepository.getSettingsManager(session).loadSettings()
        val settingsBackupTime = userPreferencesManager.preferencesForCurrentUser()?.userSettingsBackupTime
        return SyncSummaryItem(
            settings.id ?: "SETTINGS_userId",
            settingsBackupTime ?: Instant.EPOCH,
            SyncObjectType.SETTINGS
        )
    }

    fun applyBackupDate(backupTime: Instant) {
        userPreferencesManager.userSettingsBackupTimeMillis = backupTime.toEpochMilli()
    }

    fun deleteForSync() {
        error("Shouldn't delete settings")
    }

    fun clearPendingOperations() {
        userDataRepository.getSettingsManager(sessionManager.session!!).shouldSyncSettings = false
    }
}