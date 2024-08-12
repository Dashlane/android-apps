package com.dashlane.core.sync

import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ErrorDescription
import com.dashlane.hermes.generated.definitions.ErrorName
import com.dashlane.hermes.generated.definitions.ErrorStep
import com.dashlane.hermes.generated.definitions.Extent
import com.dashlane.hermes.generated.definitions.TreatProblem
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.events.user.Sync
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttp400InvalidRequestException
import com.dashlane.server.api.exceptions.DashlaneApiHttpException
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.server.api.exceptions.DashlaneApiOfflineException
import com.dashlane.session.Session
import com.dashlane.session.UserDataRepository
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.sync.repositories.ServerCredentials
import com.dashlane.sync.repositories.SyncProgressChannel
import com.dashlane.sync.repositories.SyncRepository
import com.dashlane.sync.vault.SyncVault
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.generateUniqueIdentifier
import kotlinx.coroutines.ObsoleteCoroutinesApi
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.dashlane.hermes.generated.definitions.Duration as HermesDuration

@Singleton
class DataSyncHelper @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userCryptographyRepository: UserCryptographyRepository,
    private val logRepository: LogRepository,
    private val syncVault: SyncVaultImplRaclette,
    private val syncRepository: SyncRepository,
    private val legacyCryptoMigrationHelper: LegacyCryptoMigrationHelper,
    private val syncBroadcastManager: SyncBroadcastManager,
    private val accountStatusRepository: AccountStatusRepository,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
) {

    @OptIn(ObsoleteCoroutinesApi::class)
    suspend fun runInitialSync(session: Session) {
        runSdkSync(
            session = session,
            isInitialSync = true,
            origin = Trigger.INITIAL_LOGIN,
            syncProgressChannel = DataSyncStatus.progress,
            syncRepository = syncRepository,
            syncVault = syncVault
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
            syncVault = syncVault
        )
        
        runCatching {
            legacyCryptoMigrationHelper.migrateCryptoIfNeeded(session)
        }
    }

    private suspend fun runSdkSync(
        session: Session,
        isInitialSync: Boolean,
        origin: Trigger,
        syncProgressChannel: SyncProgressChannel? = null,
        syncRepository: SyncRepository,
        syncVault: SyncVault
    ) {
        accountStatusRepository.refreshFor(session)
        val serverCredentials = session.toServerCredentials()
        val success = try {
            syncRepository.sync(
                serverCredentials,
                userCryptographyRepository.getCryptographyEngineFactory(session),
                syncVault,
                syncProgressChannel = syncProgressChannel
            )
        } catch (e: SyncRepository.SyncException) {
            handleSyncError(isInitialSync, e, origin)
            throw e
        }
        handleSyncSuccess(
            session,
            success,
            isInitialSync,
            origin,
            serverCredentials,
            syncRepository,
            syncVault
        )
    }

    private suspend fun handleSyncSuccess(
        session: Session,
        success: SyncRepository.Result,
        isInitialSync: Boolean,
        origin: Trigger,
        serverCredentials: ServerCredentials,
        syncRepository: SyncRepository,
        syncVault: SyncVault
    ) {
        sendSyncSuccessLog(isInitialSync, success, origin)
        if (success.duplicateCount > 0) {
            try {
                
                syncRepository.syncChronological(
                    serverCredentials,
                    userCryptographyRepository.getCryptographyEngineFactory(session),
                    syncVault
                )
            } catch (ignored: SyncRepository.SyncException) {
            }
        }
        syncBroadcastManager.sendSyncFinishedBroadcast(origin)
        currentTeamSpaceUiFilter.loadFilter()
    }

    private fun handleSyncError(
        isInitialSync: Boolean,
        e: SyncRepository.SyncException,
        origin: Trigger
    ) {
        sendSyncErrorLog(isInitialSync, e, origin)
        when (e.cause) {
            is DashlaneApiHttp400InvalidRequestException.UnknownUserDeviceKey -> {
                
                
                syncBroadcastManager.sendPasswordErrorBroadcast()
            }

            else -> {
                syncBroadcastManager.sendSyncFailedBroadcast(origin)
            }
        }
    }

    private fun sendSyncSuccessLog(
        isInitialSync: Boolean,
        success: SyncRepository.Result,
        origin: Trigger
    ) {
        logRepository.logSync(success, isInitialSync, origin)
    }

    private fun sendSyncErrorLog(
        isInitialSync: Boolean,
        error: SyncRepository.SyncException,
        origin: Trigger
    ) {
        logRepository.logSync(error, isInitialSync, origin)
    }
}

private fun Session.toServerCredentials() = ServerCredentials(
    login = userId,
    accessKey = accessKey,
    secretKey = secretKey,
)

@SuppressWarnings("fb-contrib:UMTP_UNBOUND_METHOD_TEMPLATE_PARAMETER")
private inline fun <reified T> Sequence<*>.anyInstanceOf(): Boolean = filterIsInstance<T>().any()

private val Throwable.causes: Sequence<Throwable>
    get() = generateSequence(this, Throwable::cause)

private fun LogRepository.logSync(
    success: SyncRepository.Result,
    isInitialSync: Boolean,
    origin: Trigger
) = logSync(
    isInitialSync = isInitialSync,
    origin = origin,
    timings = success.timings,
    duplicates = success.duplicateCount,
    incomingUpdateCount = success.statistics.incomingTransactions.updateCount,
    incomingDeleteCount = success.statistics.incomingTransactions.deleteCount,
    outgoingUpdateCount = success.statistics.outgoingTransactions.updateCount,
    outgoingDeleteCount = success.statistics.outgoingTransactions.deleteCount,
    treatProblemType = success.treatProblemType
)

private fun LogRepository.logSync(
    error: SyncRepository.SyncException,
    isInitialSync: Boolean,
    origin: Trigger
) = logSync(
    isInitialSync = isInitialSync,
    origin = origin,
    timings = error.timings,
    error = error
)

private fun LogRepository.logSync(
    isInitialSync: Boolean,
    origin: Trigger,
    timings: SyncRepository.Timings?,
    duplicates: Int? = null,
    incomingUpdateCount: Int? = null,
    incomingDeleteCount: Int? = null,
    outgoingUpdateCount: Int? = null,
    outgoingDeleteCount: Int? = null,
    treatProblemType: SyncRepository.Result.TreatProblemType? = null,
    error: SyncRepository.SyncException? = null
) {
    val errorData = error?.toErrorData()
    queueEvent(
        Sync(
            duration = timings.toHermesDuration(),
            deduplicates = duplicates,
            errorName = errorData?.name,
            errorDescription = errorData?.description,
            errorStep = errorData?.step,
            extent = if (isInitialSync) Extent.INITIAL else Extent.FULL,
            incomingUpdateCount = incomingUpdateCount,
            incomingDeleteCount = incomingDeleteCount,
            outgoingUpdateCount = outgoingUpdateCount,
            outgoingDeleteCount = outgoingDeleteCount,
            timestamp = ChronoUnit.SECONDS.between(Instant.EPOCH, timings?.sync?.start).toInt(),
            treatProblem = treatProblemType?.toHermesProblemType(),
            trigger = origin
        ).also {
        }
    )
}

private fun SyncRepository.Timings?.toHermesDuration(): HermesDuration {
    fun SyncRepository.Timing?.toMillis() = this?.duration?.toMillis()?.toInt()

    return HermesDuration(
        sync = this?.sync?.toMillis() ?: 0,
        chronological = this?.chronological?.toMillis() ?: 0,
        treatProblem = this?.treatProblem?.toMillis() ?: 0,
        sharing = this?.sharing?.toMillis() ?: 0
    )
}

private fun SyncRepository.Result.TreatProblemType.toHermesProblemType() = when (this) {
    SyncRepository.Result.TreatProblemType.DOWNLOAD -> TreatProblem.DOWNLOAD
    SyncRepository.Result.TreatProblemType.NONE -> TreatProblem.NOT_NEEDED
    SyncRepository.Result.TreatProblemType.SYNC -> TreatProblem.UPLOAD_AND_DOWNLOAD
    SyncRepository.Result.TreatProblemType.UPLOAD -> TreatProblem.UPLOAD
}

private fun SyncRepository.SyncException.toErrorData(): SyncErrorData {
    val causes = this.causes
    val (name, desc) = when {
        causes.anyInstanceOf<IOException>() || causes.anyInstanceOf<DashlaneApiIoException>() -> {
            val desc =
                if (causes.anyInstanceOf<DashlaneApiOfflineException>()) ErrorDescription.NO_NETWORK else null
            ErrorName.HTTP_IO to desc
        }

        causes.anyInstanceOf<DashlaneApiHttp400InvalidRequestException.UnknownUserDeviceKey>() ->
            ErrorName.AUTHENTICATION to null

        causes.anyInstanceOf<HttpException>() || causes.anyInstanceOf<DashlaneApiHttpException>() ->
            ErrorName.HTTP_STATUS to null

        causes.anyInstanceOf<DashlaneApiException>() ->
            ErrorName.RESPONSE_CONTENT to null

        causes.anyInstanceOf<OutOfMemoryError>() ->
            ErrorName.MEMORY to null

        else -> ErrorName.OTHER to null
    }

    val errorStep = when (step) {
        SyncRepository.SyncException.Step.CHRONOLOGICAL -> ErrorStep.CHRONOLOGICAL
        SyncRepository.SyncException.Step.DEDUPLICATION -> ErrorStep.DEDUPLICATE
        SyncRepository.SyncException.Step.SHARING -> ErrorStep.SHARING
        SyncRepository.SyncException.Step.TREAT -> ErrorStep.TREAT_PROBLEM
    }

    return SyncErrorData(name, desc, errorStep)
}

@StringRes
fun SyncFinishedEvent.getAgnosticMessageFeedback() =
    when (state) {
        SyncFinishedEvent.State.ERROR -> R.string.vault_unknown_sync_error_message
        SyncFinishedEvent.State.OFFLINE -> R.string.vault_offline_sync_error_message
        SyncFinishedEvent.State.SUCCESS -> null 
    }.takeIf { trigger == SyncFinishedEvent.Trigger.BY_USER }

data class SyncErrorData(
    val name: ErrorName,
    val description: ErrorDescription?,
    val step: ErrorStep
)
