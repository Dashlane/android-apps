package com.dashlane.settings.general.backup

import android.content.Context
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.session.SessionManager
import com.dashlane.settings.Backup
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsEvent.SettingsLoaded
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.general.backup.SettingsBackupState.View.Initial
import com.dashlane.settings.general.backup.SettingsBackupState.View.WaitingForLock
import com.dashlane.settings.ui.R
import com.dashlane.teamspaces.export.ExportVaultStatus
import com.dashlane.teamspaces.export.isEnable
import com.dashlane.teamspaces.manager.exportVaultStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take

fun loadBackupSettings(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    accountStatusRepository: AccountStatusRepository,
    state: Initial,
): Flow<SettingsEvent<Backup>> {
    return flow {

        val session = sessionManager.session ?: run {
            emit(SettingsEvent.Error(SettingsError.Generic))
            return@flow
        }
        val accountStatus = accountStatusRepository[session]
        val exportVaultStatus = accountStatus?.exportVaultStatus() ?: ExportVaultStatus.COMPLETE

        val backupSettings = mutableListOf<SettingsItemData>()

        
        backupSettings.add(
            SettingsItemData.Click(
                id = Backup.Export,
                title = context.getString(R.string.setting_backup_export),
                description = context.getString(R.string.setting_backup_export_description),
                enabled = exportVaultStatus.isEnable,
            ),
        )

        
        backupSettings.add(
            SettingsItemData.Click(
                id = Backup.ExportCsv,
                title = context.getString(R.string.setting_backup_export_csv),
                description = context.getString(R.string.setting_backup_export_csv_description),
                enabled = exportVaultStatus.isEnable,
            ),
        )

        
        backupSettings.add(
            SettingsItemData.Click(
                id = Backup.Import,
                title = context.getString(R.string.setting_backup_import),
                description = context.getString(R.string.setting_backup_import_description),
                enabled = true,
            ),
        )
        emit(SettingsLoaded(backupSettings))
    }
}

fun waitForLock(
    lockManager: LockManager,
    state: WaitingForLock,
): Flow<SettingsEvent<Backup>> {
    return lockManager.lockEventFlow
        .take(1)
        .mapNotNull { lockEvent ->
            if (lockEvent is LockEvent.Unlock) {
                SettingsEvent.Unlocked(state.sideEffect)
            } else {
                SettingsLoaded(state.data.backupSettings)
            }
        }
}