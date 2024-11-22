package com.dashlane.securearchive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.crashreport.CrashReporter
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class BackupViewModel @Inject constructor(
    private val secureArchiveManager: SecureArchiveManager,
    private val crashReporter: CrashReporter,
    savedStateHandle: SavedStateHandle
) : ViewModel(), BackupViewModelContract {

    override val operation = BackupActivityIntents.getBackupAction(savedStateHandle)

    override val state = MutableStateFlow<BackupViewState>(BackupViewState.Idle)

    override fun onPasswordChanged() {
        
        state.value = BackupViewState.Idle
    }

    override fun onBackupLaunch(password: String) {
        state.value = BackupViewState.Processing
        viewModelScope.launch {
            state.value = try {
                val items = when (operation) {
                    is BackupOperation.Import -> secureArchiveManager.import(
                        operation.uri,
                        password
                    )
                    is BackupOperation.Export -> secureArchiveManager.export(password)
                }

                BackupViewState.Success(items.countDisplayableDataType())
            } catch (t: Throwable) {
                when (t) {
                    is CancellationException -> throw t
                    is SecureArchiveManager.InvalidPassword -> {
                        BackupViewState.InvalidPasswordError
                    }
                    is SecureArchiveManager.FallbackToSharingArchive -> {
                        
                        
                        crashReporter.logNonFatal(t.originalException)
                        BackupViewState.FallbackToFileIntent(
                            t.data.countDisplayableDataType(),
                            t.cacheFile
                        )
                    }
                    else -> {
                        
                        crashReporter.logNonFatal(t)
                        BackupViewState.UnhandledError(t)
                    }
                }
            }
        }
    }

    override fun onBackupCancel() {
        state.value = BackupViewState.Cancelled
    }
}

private val displayableDataTypes = setOf(
    SyncObjectType.ADDRESS,
    SyncObjectType.COMPANY,
    SyncObjectType.EMAIL,
    SyncObjectType.IDENTITY,
    SyncObjectType.PERSONAL_WEBSITE,
    SyncObjectType.BANK_STATEMENT,
    SyncObjectType.PHONE,
    SyncObjectType.PAYMENT_CREDIT_CARD,
    SyncObjectType.DRIVER_LICENCE,
    SyncObjectType.FISCAL_STATEMENT,
    SyncObjectType.ID_CARD,
    SyncObjectType.PASSPORT,
    SyncObjectType.SOCIAL_SECURITY_STATEMENT,
    SyncObjectType.SECURE_NOTE,
    SyncObjectType.AUTHENTIFIANT
)

private fun List<VaultItem<*>>.countDisplayableDataType() =
    count { it.syncObjectType in displayableDataTypes }
