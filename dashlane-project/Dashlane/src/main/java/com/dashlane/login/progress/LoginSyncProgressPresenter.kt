package com.dashlane.login.progress

import android.app.Activity
import android.content.Intent
import com.dashlane.R
import com.dashlane.async.BroadcastManager
import com.dashlane.core.sync.DataSyncStatus
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.login.Device
import com.dashlane.login.LoginActivity
import com.dashlane.login.LoginIntents
import com.dashlane.login.pages.password.InitialSync
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.devices.DeactivateDevicesService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.sync.repositories.SyncProgress
import com.dashlane.util.Constants
import com.dashlane.util.clearTask
import com.dashlane.util.coroutines.DeferredViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("EXPERIMENTAL_API_USAGE")
class LoginSyncProgressPresenter(
    private val activity: Activity,
    private val session: Session,
    private val viewProxy: LoginSyncProgressContract.ViewProxy,
    private val initialSync: InitialSync,
    private val devicesToUnregister: List<Device>,
    private val deactivateDevicesService: DeactivateDevicesService,
    private val viewModel: DeferredViewModel<Unit>,
    coroutineScope: CoroutineScope
) : LoginSyncProgressContract.Presenter, CoroutineScope by coroutineScope {

    private val finalizationActor = actor<Boolean>(Dispatchers.Main.immediate, Channel.UNLIMITED) {
        var finalizationProgressJob: Job? = null
        var progress = CHRONOLOGICAL_SYNC_PERCENT_HALF * 2
        for (command in this) {
            viewProxy.setProgress(progress)
            if (command) {
                
                finalizationProgressJob?.cancelAndJoin()

                
                val percentagePointsRemaining = 100 - progress
                repeat(percentagePointsRemaining) {
                    delay(FINALIZATION_PERCENT_STEP_INITIAL_DURATION_MILLIS)
                    progress++
                    viewProxy.setProgress(progress)
                }
                startDashlane()

                
                
                
                delay(Long.MAX_VALUE)
            } else {
                if (finalizationProgressJob == null) { 
                    
                    finalizationProgressJob = launch(Dispatchers.Main.immediate) {
                        var stepDurationMillis = FINALIZATION_PERCENT_STEP_INITIAL_DURATION_MILLIS
                        repeat(FINALIZATION_PERCENT) {
                            delay(stepDurationMillis)
                            progress++
                            stepDurationMillis = stepDurationMillis * 6 / 5
                            viewProxy.setProgress(progress)
                        }
                    }
                }
            }
        }
    }

    init {
        setupAsync()
    }

    override fun retry() = setupAsync()

    private fun setProgress(syncProgress: SyncProgress) {
        val (progress, message) = when (syncProgress) {
            is SyncProgress.DecipherRemote ->
                (syncProgress.progress * CHRONOLOGICAL_SYNC_PERCENT_HALF).toInt() to
                        activity.getString(R.string.login_sync_progress_deciphering)
            is SyncProgress.LocalSync ->
                (CHRONOLOGICAL_SYNC_PERCENT_HALF + syncProgress.progress * CHRONOLOGICAL_SYNC_PERCENT_HALF).toInt() to
                        activity.getString(R.string.login_sync_progress_saving)
            SyncProgress.TreatProblem -> {
                viewProxy.setMessage(activity.getString(R.string.login_sync_progress_finalizing))
                finalizationActor.trySend(false)
                return
            }
            else ->
                
                return
        }

        viewProxy.setProgress(progress)
        viewProxy.setMessage(message)
    }

    private fun setCompletionSuccess() {
        BroadcastManager.removeBufferedIntentFor(BroadcastManager.Broadcasts.PasswordBroadcast)
        finalizationActor.trySend(true)
    }

    private suspend fun setCompletionError() {
        val sessionManager = SingletonProvider.getComponent().sessionManager
        sessionManager.session?.let { sessionManager.destroySession(it, byUser = false, forceLogout = false) }

        val intent = Intent(activity, LoginActivity::class.java).apply {
            clearTask()
            putExtra(LoginActivity.SYNC_ERROR, true)
        }
        viewProxy.finish(intent)
    }

    private fun startDashlane() {
        if (LoginIntents.shouldCloseLoginAfterSuccess(activity.intent)) {
            viewProxy.finish()
            return
        }

        val mainIntent = LoginIntents.createHomeActivityIntent(activity)

        Constants.TIME.LOGIN_TIME_SECONDS = System.currentTimeMillis() / 1000

        viewProxy.finish(mainIntent)
    }

    private fun setupAsync() {
        
        val deferred = viewModel.deferred ?: viewModel.async {
            with(initialSync) {
                if (unregisterDevices()) {
                    launchInitialSync()
                } else {
                    throw IllegalStateException("Selected devices have not been unregistered")
                }
            }
        }

        
        launch(Dispatchers.Main.immediate) {
            DataSyncStatus.progress.openSubscription().consumeEach(::setProgress)
        }

        
        launch(Dispatchers.Main.immediate) {
            try {
                deferred.await()
            } catch (e: CancellationException) {
                
                return@launch
            } catch (e: IllegalStateException) {
                
                viewModel.deferred = null
                return@launch
            } catch (e: Exception) {
                setCompletionError()
                return@launch
            }
            setCompletionSuccess()
        }
    }

    private suspend fun unregisterDevices(): Boolean {
        if (devicesToUnregister.isNotEmpty()) {
            val devices = devicesToUnregister.filter { it.pairingGroupId == null }
            val groups = devicesToUnregister.minus(devices)
            try {
                deactivateDevicesService.execute(
                    session.authorization,
                    DeactivateDevicesService.Request(
                        deviceIds = devices.map { it.id },
                        pairingGroupIds = groups.map { it.pairingGroupId!! }
                    )
                )
            } catch (e: DashlaneApiException) {
                viewProxy.showUnlinkError()
                return false
            }
        }
        return true
    }

    companion object {
        private const val CHRONOLOGICAL_SYNC_PERCENT_HALF = 40

        private const val FINALIZATION_PERCENT = 20

        private const val FINALIZATION_PERCENT_STEP_INITIAL_DURATION_MILLIS = 100L
    }
}