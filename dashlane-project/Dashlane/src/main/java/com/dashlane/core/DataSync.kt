package com.dashlane.core

import com.dashlane.async.BroadcastManager
import com.dashlane.breach.BreachManager
import com.dashlane.core.sync.DataSyncHelper
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.logger.utils.LogsSender
import com.dashlane.login.LoginInfo
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionObserver
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.teamspaces.manager.SpaceDeletedNotifier
import com.dashlane.util.AppSync
import com.dashlane.util.NetworkStateProvider
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
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
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class, FlowPreview::class)
@Singleton
class DataSync @Inject constructor(
    @IoCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher,
    private val networkStateProvider: NetworkStateProvider,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    private val syncHelper: DataSyncHelper,
    private val sessionManager: SessionManager,
    private val spaceDeletedNotifier: SpaceDeletedNotifier,
    private val teamspaceForceCategorizationManager: TeamspaceForceCategorizationManager,
    private val breachManager: BreachManager,
    private val dataSyncNotification: DataSyncNotification,
    private val logsSender: LogsSender
) : AppSync, SessionObserver {
    private var syncBlocked = false

    
    private val commandsFlow = SyncCommandFlow<Trigger>()

    
    private val forceCommandsFlow = SyncCommandFlow<Trigger>()

    
    private val _dataSyncState: MutableSharedFlow<DataSyncState> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val dataSyncState: SharedFlow<DataSyncState> = _dataSyncState.asSharedFlow()

    val latestDataSyncState: DataSyncState?
        get() = dataSyncState.replayCache.lastOrNull()

    var lastSyncDuration: Duration = Duration.ZERO
        private set

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        super.sessionStarted(session, loginInfo)
        val coroutineScope: CoroutineScope = sessionCoroutineScopeRepository[session] ?: return

        merge(
            
            commandsFlow.throttleLatest(SYNC_REFRESH_INTERVAL),
            forceCommandsFlow
        )
            .flatMapMerge { command -> syncFlow(command) }
            .onStart {
                emit(DataSyncState.Idle.Init)
                maySync()
            }
            .catch { error -> emit(DataSyncState.Idle.Failure(error)) }
            .onEach { state ->
                _dataSyncState.emit(state) 
            }
            .flowOn(dispatcher)
            .launchIn(coroutineScope)
    }

    fun sync(origin: Trigger = Trigger.SAVE) {
        forceCommandsFlow.tryEmit(origin)
    }

    suspend fun awaitSync(origin: Trigger = Trigger.SAVE):
            Boolean {
        _dataSyncState.resetReplayCache()
        forceCommandsFlow.emit(origin)
        return _dataSyncState.filterIsInstance<DataSyncState.Idle>()
            .first() == DataSyncState.Idle.Success
    }

    fun maySync() = commandsFlow.tryEmit(Trigger.WAKE)

    override fun sync() = sync(Trigger.SAVE)

    private fun syncFlow(origin: Trigger) = flow {
        if (syncBlocked) {
            return@flow
        }
        val session = sessionManager.session ?: return@flow
        dataSyncNotification.showSyncNotification()
        emit(DataSyncState.Active)
        syncInternal(session, origin)
            .onSuccess {
                onSyncSuccess(session)
                emit(DataSyncState.Idle.Success)
            }
            .onFailure { throwable ->
                onSyncFailure(throwable, origin)
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

    private suspend fun runSync(session: Session, origin: Trigger) =
        runCatching {
            lastSyncDuration = measureTime {
                syncHelper.runSync(session, origin)
            }
        }

    fun markSyncAllowed() {
        syncBlocked = false
    }

    fun markSyncNotAllowed() {
        syncBlocked = true
    }

    private suspend fun onSyncSuccess(session: Session) {
        
        teamspaceForceCategorizationManager.executeSync()
        spaceDeletedNotifier.sendIfNeeded(session)
        breachManager.refreshIfNecessary(false)
        dataSyncNotification.hideSyncNotification()
        logsSender.flushLogs()
    }

    private fun onSyncFailure(exception: Throwable, origin: Trigger) {
        dataSyncNotification.hideSyncNotification()
        if (exception is OfflineException) BroadcastManager.sendOfflineSyncFailedBroadcast(origin)
        logsSender.flushLogs()
    }

    sealed class DataSyncState {
        sealed class Idle : DataSyncState() {
            object Init : Idle()
            class Failure(val exception: Throwable) : Idle()
            object Success : Idle()
        }

        object Active : DataSyncState()
    }

    object OfflineException : Exception()

    companion object {
        private val SYNC_REFRESH_INTERVAL = 5.minutes 
    }
}
