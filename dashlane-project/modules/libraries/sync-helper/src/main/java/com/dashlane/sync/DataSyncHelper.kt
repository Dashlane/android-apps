package com.dashlane.sync

import androidx.annotation.StringRes
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.debug.SyncDebugLogger
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.server.api.exceptions.DashlaneApiHttp400InvalidRequestException
import com.dashlane.session.Session
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.sync.repositories.ServerCredentials
import com.dashlane.sync.repositories.SyncRepository
import com.dashlane.sync.vault.SyncVault
import com.dashlane.synchelper.R
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.util.generateUniqueIdentifier
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncHelper @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userCryptographyRepository: UserCryptographyRepository,
    private val syncVault: SyncVaultImplRaclette,
    private val syncRepository: SyncRepository,
    private val legacyCryptoMigrationHelper: LegacyCryptoMigrationHelper,
    private val syncBroadcastManager: SyncBroadcastManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    private val syncDebugLogger: SyncDebugLogger,
) {
    val lastSyncTime: Instant?
        get() = syncVault.lastSyncTime

    suspend fun runInitialSync(session: Session, dataSyncState: MutableSharedFlow<DataSyncState>) {
        runSdkSync(
            session = session,
            isInitialSync = true,
            origin = Trigger.INITIAL_LOGIN,
            dataSyncState = dataSyncState,
            syncRepository = syncRepository,
            syncVault = syncVault,
        )

        
        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings()
        settings.anonymousUserId.takeUnless { it.isNullOrEmpty() }
            ?: generateUniqueIdentifier().also {
                settingsManager.updateSettings(settings.copy { anonymousUserId = it }, true)
            }
    }

    suspend fun runSync(session: Session, origin: Trigger) {
        runSdkSync(
            session = session,
            isInitialSync = false,
            origin = origin,
            syncRepository = syncRepository,
            syncVault = syncVault,
        )
        
        runCatching {
            legacyCryptoMigrationHelper.migrateCryptoIfNeeded(session)
        }
    }

    private suspend fun runSdkSync(
        session: Session,
        isInitialSync: Boolean,
        origin: Trigger,
        dataSyncState: MutableSharedFlow<DataSyncState>? = null,
        syncRepository: SyncRepository,
        syncVault: SyncVault,
    ) {
        accountStatusRepository.refreshFor(session)
        val serverCredentials = session.toServerCredentials()
        val success = try {
            syncRepository.sync(
                serverCredentials = serverCredentials,
                cryptographyEngineFactory = userCryptographyRepository
                    .getCryptographyEngineFactory(session),
                vault = syncVault,
                dataSyncState = dataSyncState,
            )
        } catch (e: SyncRepository.SyncException) {
            handleSyncError(isInitialSync, e, origin)
            throw e
        }
        handleSyncSuccess(
            session = session,
            success = success,
            isInitialSync = isInitialSync,
            origin = origin,
            serverCredentials = serverCredentials,
            syncRepository = syncRepository,
            syncVault = syncVault,
        )
    }

    private suspend fun handleSyncSuccess(
        session: Session,
        success: SyncRepository.Result,
        isInitialSync: Boolean,
        origin: Trigger,
        serverCredentials: ServerCredentials,
        syncRepository: SyncRepository,
        syncVault: SyncVault,
    ) {
        sendSyncSuccessLog(isInitialSync, success, origin)
        if (success.duplicateCount > 0) {
            try {
                
                syncRepository.syncChronological(
                    serverCredentials,
                    userCryptographyRepository.getCryptographyEngineFactory(session),
                    syncVault,
                )
            } catch (ignored: SyncRepository.SyncException) {
            }
        }
        currentTeamSpaceUiFilter.loadFilter()
    }

    private fun handleSyncError(
        isInitialSync: Boolean,
        e: SyncRepository.SyncException,
        origin: Trigger,
    ) {
        sendSyncErrorLog(isInitialSync, e, origin)
        when (e.cause) {
            is DashlaneApiHttp400InvalidRequestException.UnknownUserDeviceKey -> {
                
                
                syncBroadcastManager.sendPasswordErrorBroadcast()
            }
        }
    }

    private fun sendSyncSuccessLog(
        isInitialSync: Boolean,
        success: SyncRepository.Result,
        origin: Trigger,
    ) {
        syncDebugLogger.logSync(success, isInitialSync, origin)
    }

    private fun sendSyncErrorLog(
        isInitialSync: Boolean,
        error: SyncRepository.SyncException,
        origin: Trigger,
    ) {
        syncDebugLogger.logSync(error, isInitialSync, origin)
    }
}

private fun Session.toServerCredentials() = ServerCredentials(
    login = userId,
    accessKey = accessKey,
    secretKey = secretKey,
)

@StringRes
fun DataSyncState.Idle.Failure.getAgnosticMessageFeedback() =
    when (exception) {
        is OfflineException -> R.string.vault_offline_sync_error_message
        else -> R.string.vault_unknown_sync_error_message
    }.takeIf { trigger == Trigger.MANUAL }