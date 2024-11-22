package com.dashlane.core

import com.dashlane.async.SyncBroadcastManager
import com.dashlane.breach.BreachManager
import com.dashlane.core.sync.PaymentPaypalMigrationHelper
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.login.LoginInfo
import com.dashlane.login.LoginMode
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionObserver
import com.dashlane.session.authorization
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncHelper
import com.dashlane.sync.DataSyncState
import com.dashlane.sync.repositories.SyncProgress
import com.dashlane.teamspaces.db.SmartSpaceCategorizationManager
import com.dashlane.teamspaces.manager.SpaceDeletedNotifier
import com.dashlane.useractivity.UserActivitySender
import com.dashlane.util.NetworkStateProvider
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class DataSyncImpl @Inject constructor(
    @IoCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher,
    private val networkStateProvider: NetworkStateProvider,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val syncHelper: DataSyncHelper,
    private val sessionManager: SessionManager,
    private val spaceDeletedNotifier: SpaceDeletedNotifier,
    private val smartSpaceCategorizationManager: SmartSpaceCategorizationManager,
    private val breachManager: BreachManager,
    private val dataSyncNotification: DataSyncNotification,
    private val logRepository: LogRepository,
    private val syncBroadcastManager: SyncBroadcastManager,
    private val userActivitySender: UserActivitySender,
    private val paymentPaypalMigrationHelper: PaymentPaypalMigrationHelper
) : SessionObserver, DataSync {
    private var syncBlocked = false

    
    private val commandsFlow = SyncCommandFlow<Command>()

    
    private val forceCommandsFlow = SyncCommandFlow<Command>()

    
    private val _dataSyncState: MutableSharedFlow<DataSyncState> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val dataSyncState: SharedFlow<DataSyncState> = _dataSyncState.asSharedFlow()

    val latestDataSyncState: DataSyncState?
        get() = dataSyncState.replayCache.lastOrNull()

    private var lastSyncDuration: Duration = Duration.ZERO

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        super.sessionStarted(session, loginInfo)
        val coroutineScope: CoroutineScope = sessionCoroutineScopeRepository[session] ?: return

        merge(
            
            commandsFlow.throttleLatest(SYNC_REFRESH_INTERVAL),
            forceCommandsFlow
        )
            .flatMapMerge(concurrency = 1) { command -> syncFlow(command) }
            .onStart {
                emit(DataSyncState.Idle.Init)
                
                if (loginInfo?.isFirstLogin == true &&
                    loginInfo.loginMode != LoginMode.SessionRestore
                ) {
                    return@onStart
                }
                
                maySync()
            }
            .catch { error -> emit(DataSyncState.Idle.Failure(error)) }
            .onEach { state ->
                _dataSyncState.emit(state) 
            }
            .flowOn(dispatcher)
            .launchIn(coroutineScope)
    }

    override suspend fun initialSync() {
        forceCommandsFlow.tryEmit(Command.InitialSync)
    }

    override fun sync(origin: Trigger) {
        forceCommandsFlow.tryEmit(Command.Sync(origin))
    }

    override suspend fun awaitSync(origin: Trigger): Boolean {
        _dataSyncState.resetReplayCache()
        forceCommandsFlow.emit(Command.Sync(origin))
        return _dataSyncState
            .filter { state -> state is DataSyncState.Idle.Success || state is DataSyncState.Idle.Failure }
            .first() == DataSyncState.Idle.Success
    }

    override fun maySync() = commandsFlow.tryEmit(Command.Sync(Trigger.WAKE))

    override fun markSyncAllowed() {
        syncBlocked = false
    }

    override fun markSyncNotAllowed() {
        syncBlocked = true
    }

    private fun syncFlow(command: Command) = flow {
        if (syncBlocked) {
            return@flow
        }
        val session = sessionManager.session ?: return@flow
        dataSyncNotification.showSyncNotification()
        emit(DataSyncState.Active(SyncProgress.Start))
        when (command) {
            Command.InitialSync -> syncInternalInitial(session = session, dataSyncState = _dataSyncState)
            is Command.Sync -> syncInternal(session, command.trigger)
        }.onSuccess {
            onSyncSuccess(session)
            emit(DataSyncState.Idle.Success)
        }.onFailure { throwable ->
            onSyncFailure(throwable, (command as? Command.Sync)?.trigger ?: Trigger.LOGIN)
            emit(DataSyncState.Idle.Failure(throwable))
        }
    }

    private suspend fun syncInternal(
        session: Session,
        origin: Trigger
    ): Result<Unit> = if (networkStateProvider.isOn()) {
        runSync(session, origin)
    } else {
        Result.failure(OfflineException)
    }

    private suspend fun syncInternalInitial(
        session: Session,
        dataSyncState: MutableSharedFlow<DataSyncState>
    ): Result<Unit> = if (networkStateProvider.isOn()) {
        runCatching {
            syncHelper.runInitialSync(session, dataSyncState)
        }
    } else {
        Result.failure(OfflineException)
    }

    private suspend fun runSync(session: Session, origin: Trigger) =
        runCatching {
            lastSyncDuration = measureTime {
                syncHelper.runSync(session, origin)
            }
        }

    private suspend fun onSyncSuccess(session: Session) {
        
        paymentPaypalMigrationHelper.migratePaymentPaypal()
        smartSpaceCategorizationManager.executeSync()
        spaceDeletedNotifier.sendIfNeeded(session)
        breachManager.refreshIfNecessary(false)
        dataSyncNotification.hideSyncNotification()
        userActivitySender.sendIfNeeded()
        logRepository.flushLogs(session.authorization)
    }

    private suspend fun onSyncFailure(exception: Throwable, origin: Trigger) {
        dataSyncNotification.hideSyncNotification()
        if (exception is OfflineException) {
            syncBroadcastManager.sendOfflineSyncFailedBroadcast(origin)
        }
        logRepository.flushLogs(null)
    }

    object OfflineException : Exception()

    companion object {
        private val SYNC_REFRESH_INTERVAL = 5.minutes 
    }

    sealed class Command {
        data class Sync(val trigger: Trigger) : Command()

        data object InitialSync : Command()
    }
}
