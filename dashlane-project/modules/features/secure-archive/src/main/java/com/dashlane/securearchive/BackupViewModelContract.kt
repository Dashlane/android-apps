package com.dashlane.securearchive

import kotlinx.coroutines.flow.Flow

interface BackupViewModelContract {
    val operation: BackupOperation

    val state: Flow<BackupViewState>

    fun onPasswordChanged()

    fun onBackupLaunch(password: String)

    fun onBackupCancel()
}