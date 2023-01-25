package com.dashlane.core.sync

import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.async.BroadcastManager
import com.dashlane.core.premium.PremiumStatusManager
import com.dashlane.cryptography.CryptographyMarkerException
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.dagger.sync.SyncDataStorageComponent
import com.dashlane.dagger.sync.SyncDataStorageComponentProvider
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ErrorDescription
import com.dashlane.hermes.generated.definitions.ErrorName
import com.dashlane.hermes.generated.definitions.ErrorStep
import com.dashlane.hermes.generated.definitions.Extent
import com.dashlane.hermes.generated.definitions.TreatProblem
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.hermes.generated.events.user.Sync
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttp400InvalidRequestException
import com.dashlane.server.api.exceptions.DashlaneApiHttpException
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.server.api.exceptions.DashlaneApiOfflineException
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.repository.UserCryptographyRepository
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.storage.DataStorageMigrationHelper
import com.dashlane.sync.DaggerSyncComponent
import com.dashlane.sync.SyncComponent
import com.dashlane.sync.SyncLogsModule
import com.dashlane.sync.domain.SyncCryptographyException
import com.dashlane.sync.domain.SyncTransactionException
import com.dashlane.sync.repositories.ServerCredentials
import com.dashlane.sync.repositories.SyncProgressChannel
import com.dashlane.sync.repositories.SyncRepository
import com.dashlane.sync.sharing.SharingComponent
import com.dashlane.sync.sharing.SharingSync
import com.dashlane.sync.util.SyncLogsWriter
import com.dashlane.sync.vault.SyncVault
import com.dashlane.useractivity.DeveloperInfoLogger
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode134
import com.dashlane.useractivity.log.usage.UsageLogCode135
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.SecureException
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.util.logI
import com.dashlane.xml.XmlTypeException
import com.dashlane.xml.serializer.XmlException
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.zip.ZipException
import javax.inject.Inject
import javax.inject.Provider
import com.dashlane.hermes.generated.definitions.Duration as HermesDuration



class DataSyncHelper @Inject constructor(
    private val dataStorageMigrationHelper: Provider<DataStorageMigrationHelper>,
    private val userDataRepository: UserDataRepository,
    private val userCryptographyRepository: UserCryptographyRepository,
    private val premiumStatusManager: PremiumStatusManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository,
    userPreferencesManager: UserPreferencesManager,
    userSupportFileLogger: UserSupportFileLogger,
    developerInfoLogger: DeveloperInfoLogger
) {
    val logsWriter = SyncLogsWriter(userSupportFileLogger)

    private val syncDataStorageComponent: SyncDataStorageComponent = SyncDataStorageComponentProvider.getForDefault()
    private val syncDataStorageRacletteComponent: SyncDataStorageComponent =
        SyncDataStorageComponentProvider.getForRaclette()

    private val syncComponentBuilder: DaggerSyncComponent.Builder =
        DaggerSyncComponent.builder()
            .dashlaneApiComponent(SingletonProvider.getComponent())
            .cryptographyComponent(SingletonProvider.getComponent())
            .sharingCryptographyComponent(SingletonProvider.getComponent())
            .sharingKeysHelperComponent(SingletonProvider.getComponent())
            .syncLogsModule(SyncLogsModule(logsWriter))

    val syncComponent: SyncComponent = syncComponentBuilder
        .sharingComponent(object : SharingComponent {
            override val sharingSync: SharingSync = syncDataStorageComponent.sharingSync
        })
        .build()

    private val legacyCryptoMigrationHelper = LegacyCryptoMigrationHelper(
        syncComponent,
        userDataRepository,
        userPreferencesManager,
        logRepository,
        developerInfoLogger
    )

    @OptIn(ObsoleteCoroutinesApi::class)
    suspend fun runInitialSync(session: Session) {
        runSdkSync(
            session = session,
            isInitialSync = true,
            origin = UsageLogCode134.Origin.INITIAL_LOGIN,
            syncProgressChannel = DataSyncStatus.progress,
            syncRepository = syncComponent.syncRepository,
            syncVault = syncDataStorageComponent.dataStorageProvider.syncVault
        )

        
        val settingsManager = userDataRepository.getSettingsManager(session)
        val settings = settingsManager.getSettings()
        settings.anonymousUserId.takeUnless { it.isNullOrEmpty() }
            ?: generateUniqueIdentifier().also {
                settingsManager.updateSettings(settings.copy { anonymousUserId = it }, true)
            }
    }

    fun runSync(session: Session, origin: UsageLogCode134.Origin) =
        runBlocking {
            val helper = dataStorageMigrationHelper.get()
            helper.logShouldStartMigrationPreSync()
            runSdkSync(
                session = session,
                isInitialSync = false,
                origin = origin,
                syncRepository = syncComponent.syncRepository,
                syncVault = syncDataStorageComponent.dataStorageProvider.syncVault
            )
            helper.logShouldStartMigrationPreCrypto()
            
            runCatching {
                legacyCryptoMigrationHelper.migrateCryptoIfNeeded(session)
            }
            coroutineScope {
                helper.migration(this) {
                    runSdkSyncRaclette(session, origin)
                }
            }
        }

    private suspend fun runSdkSyncRaclette(session: Session, origin: UsageLogCode134.Origin) {
        val syncComponentRaclette = syncComponentBuilder
            .sharingComponent(object : SharingComponent {
                override val sharingSync: SharingSync = syncDataStorageRacletteComponent.sharingSync
            })
            .build()

        runSdkSync(
            session = session,
            isInitialSync = false,
            origin = origin,
            syncRepository = syncComponentRaclette.syncRepository,
            syncVault = syncDataStorageRacletteComponent.dataStorageProvider.syncVault
        )
    }

    private suspend fun runSdkSync(
        session: Session,
        isInitialSync: Boolean,
        origin: UsageLogCode134.Origin,
        syncProgressChannel: SyncProgressChannel? = null,
        syncRepository: SyncRepository,
        syncVault: SyncVault
    ) {
        premiumStatusManager.refreshPremiumStatus(session)
        val serverCredentials = session.toServerCredentials()
        val success = try {
            syncRepository.sync(
                serverCredentials,
                userCryptographyRepository.getCryptographyEngineFactory(session),
                syncVault,
                syncProgressChannel = syncProgressChannel
            )
        } catch (e: SyncRepository.SyncException) {
            handleSyncError(session, isInitialSync, e, origin, syncVault)
            dataStorageMigrationHelper.get().localMigration()
            throw e
        }
        handleSyncSuccess(session, success, isInitialSync, origin, serverCredentials, syncRepository, syncVault)
    }

    private suspend fun handleSyncSuccess(
        session: Session,
        success: SyncRepository.Result,
        isInitialSync: Boolean,
        origin: UsageLogCode134.Origin,
        serverCredentials: ServerCredentials,
        syncRepository: SyncRepository,
        syncVault: SyncVault
    ) {
        sendSyncSuccessLog(session, isInitialSync, success, origin)
        for (ignoredError in success.transactionErrors) {

            if (ignoredError is SyncTransactionException) {
                sendSyncTransactionErrorLog(session, success.timings.sync, ignoredError)
            }
        }
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
        BroadcastManager.sendSyncFinishedBroadcast(origin)
    }

    private fun handleSyncError(
        session: Session,
        isInitialSync: Boolean,
        e: SyncRepository.SyncException,
        origin: UsageLogCode134.Origin,
        syncVault: SyncVault
    ) {
        sendSyncErrorLog(session, isInitialSync, e, origin, syncVault)
        when (e.cause) {
            is DashlaneApiHttp400InvalidRequestException.UnknownUserDeviceKey -> {
                
                
                BroadcastManager.sendPasswordErrorBroadcast()
            }
            else -> {
                BroadcastManager.sendSyncFailedBroadcast(origin)
            }
        }
    }

    private fun sendSyncSuccessLog(
        session: Session,
        isInitialSync: Boolean,
        success: SyncRepository.Result,
        origin: UsageLogCode134.Origin
    ) {
        send(
            session,
            UsageLogCode134(
                syncType = if (isInitialSync) UsageLogCode134.SyncType.INITIAL else UsageLogCode134.SyncType.FULL,
                syncTimestamp = getSyncTimestampForLog(success.timings.sync),
                origin = origin,
                duration = success.timings.sync.duration.toMillis(),
                durationChronological = success.timings.chronological?.duration?.toMillis() ?: 0L,
                durationTreat = success.timings.treatProblem?.duration?.toMillis() ?: 0L,
                durationSharing = success.timings.sharing?.duration?.toMillis() ?: 0L,
                incomingUpdateCount = success.statistics.incomingTransactions.updateCount.toLong(),
                incomingDeleteCount = success.statistics.incomingTransactions.deleteCount.toLong(),
                outgoingUpdateCount = success.statistics.outgoingTransactions.updateCount.toLong(),
                outgoingDeleteCount = success.statistics.outgoingTransactions.deleteCount.toLong(),
                treatProblem = success.treatProblemType.asLogConstant(),
                deduplicates = success.duplicateCount.toLong()
            )
        )
        logRepository.logSync(success, isInitialSync, origin)
    }

    private fun sendSyncErrorLog(
        session: Session,
        isInitialSync: Boolean,
        error: SyncRepository.SyncException,
        origin: UsageLogCode134.Origin,
        syncVault: SyncVault
    ) {
        send(
            session,
            UsageLogCode134(
                syncType = if (isInitialSync) UsageLogCode134.SyncType.INITIAL else UsageLogCode134.SyncType.FULL,
                origin = origin,
                errorStep = error.step.asLogConstant(),
                errorType = error.asLogErrorTypeConstant(),
                errorSubtype = error.asLogErrorSubType()
            )
        )
        logRepository.logSync(error, isInitialSync, origin)
        dataStorageMigrationHelper.get().logSyncFailIfLegacyDatabase(error, syncVault
            .lastSyncTime ?: Instant.EPOCH)
    }

    private fun SyncRepository.Result.TreatProblemType.asLogConstant(): UsageLogCode134.TreatProblem =
        when (this) {
            SyncRepository.Result.TreatProblemType.NONE -> UsageLogCode134.TreatProblem.NOT_NEEDED
            SyncRepository.Result.TreatProblemType.DOWNLOAD -> UsageLogCode134.TreatProblem.DOWNLOAD
            SyncRepository.Result.TreatProblemType.UPLOAD -> UsageLogCode134.TreatProblem.UPLOAD
            SyncRepository.Result.TreatProblemType.SYNC -> UsageLogCode134.TreatProblem.UPLOAD_AND_DOWNLOAD
        }

    private fun SyncRepository.SyncException.Step.asLogConstant(): UsageLogCode134.ErrorStep =
        when (this) {
            SyncRepository.SyncException.Step.CHRONOLOGICAL -> UsageLogCode134.ErrorStep.CHRONOLOGICAL
            SyncRepository.SyncException.Step.TREAT -> UsageLogCode134.ErrorStep.TREAT_PROBLEM
            SyncRepository.SyncException.Step.DEDUPLICATION -> UsageLogCode134.ErrorStep.DEDUPLICATE
            SyncRepository.SyncException.Step.SHARING -> UsageLogCode134.ErrorStep.SHARING
        }

    private fun SyncRepository.SyncException.asLogErrorTypeConstant(): UsageLogCode134.ErrorType {
        val causes = this.causes
        return when {
            causes.anyInstanceOf<IOException>() || causes.anyInstanceOf<DashlaneApiIoException>() ->
                UsageLogCode134.ErrorType.HTTP_IO
            causes.anyInstanceOf<DashlaneApiHttp400InvalidRequestException.UnknownUserDeviceKey>() ->
                UsageLogCode134.ErrorType.AUTHENTICATION
            causes.anyInstanceOf<HttpException>() || causes.anyInstanceOf<DashlaneApiHttpException>() ->
                UsageLogCode134.ErrorType.HTTP_STATUS
            causes.anyInstanceOf<DashlaneApiException>() ->
                UsageLogCode134.ErrorType.RESPONSE_CONTENT
            causes.anyInstanceOf<OutOfMemoryError>() ->
                UsageLogCode134.ErrorType.MEMORY
            causes.any { it.stackTrace.any { element -> element.className == SyncVaultImplLegacy::class.java.name } } ->
                UsageLogCode134.ErrorType.DATABASE
            else ->
                UsageLogCode134.ErrorType.OTHER
        }
    }

    private fun SyncRepository.SyncException.asLogErrorSubType(): String? {
        val causes = this.causes
        val secureException = causes.firstInstanceOfOrNull<SecureException>()
        if (secureException != null) return secureException.message
        val httpException = causes.firstInstanceOfOrNull<DashlaneApiHttpException>()
        if (httpException != null) return "${httpException.httpStatusCode} ${httpException.errorCode}"
        val apiException = causes.firstInstanceOfOrNull<DashlaneApiException>()
        if (apiException != null) return apiException.message
        val memoryError = causes.firstInstanceOfOrNull<OutOfMemoryError>()
        if (memoryError != null) return memoryError.stackTrace.firstOrNull()?.className
        return null
    }

    private fun sendSyncTransactionErrorLog(
        session: Session,
        syncTiming: SyncRepository.Timing,
        exception: SyncTransactionException
    ) = send(
        session,
        UsageLogCode135(
            syncTimestamp = getSyncTimestampForLog(syncTiming),
            transactionType = exception.syncObjectType.transactionType,
            transactionDate = exception.transaction.date,
            cryptoPayload = exception.cryptoExceptionPayload(),
            errorType = exception.asErrorType(),
            errorSubtype = exception.exceptionInfo()
        )
    )

    

    private fun getSyncTimestampForLog(syncTiming: SyncRepository.Timing) =
        Instant.ofEpochSecond(syncTiming.start.toEpochMilli())

    private fun SyncTransactionException.asErrorType(): UsageLogCode135.ErrorType? {
        val causes = this.causes
        return when {
            causes.anyInstanceOf<ZipException>() -> UsageLogCode135.ErrorType.ZIP
            causes.anyInstanceOf<CryptographyMarkerException>() -> UsageLogCode135.ErrorType.CRYPTO_CONFIG
            causes.anyInstanceOf<SyncCryptographyException>() -> UsageLogCode135.ErrorType.DECIPHER
            causes.anyInstanceOf<XmlException>() -> UsageLogCode135.ErrorType.XML_PARSE
            causes.anyInstanceOf<XmlTypeException>() -> UsageLogCode135.ErrorType.XML_READ
            else -> null
        }
    }

    private fun SyncTransactionException.cryptoExceptionPayload() =
        (cause as? SyncCryptographyException)?.cipherPayload

    private fun SyncTransactionException.exceptionInfo() = when (val cause = cause) {
        is XmlException -> cause.xmlErrorType
        else -> null
    }

    private fun send(session: Session, log: UsageLog) {
        bySessionUsageLogRepository[session]
            ?.enqueue(log)
    }
}

private fun Session.toServerCredentials() = ServerCredentials(
    login = userId,
    accessKey = accessKey,
    secretKey = secretKey,
    uki = uki
)

@SuppressWarnings("fb-contrib:UMTP_UNBOUND_METHOD_TEMPLATE_PARAMETER")
private inline fun <reified T> Sequence<*>.anyInstanceOf(): Boolean = filterIsInstance<T>().any()

private inline fun <reified T> Sequence<*>.firstInstanceOfOrNull(): T? =
    filterIsInstance<T>().firstOrNull()

private val Throwable.causes: Sequence<Throwable>
    get() = generateSequence(this, Throwable::cause)

private fun LogRepository.logSync(
    success: SyncRepository.Result,
    isInitialSync: Boolean,
    origin: UsageLogCode134.Origin
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
    origin: UsageLogCode134.Origin
) = logSync(
    isInitialSync = isInitialSync,
    origin = origin,
    timings = error.timings,
    error = error
)

private fun LogRepository.logSync(
    isInitialSync: Boolean,
    origin: UsageLogCode134.Origin,
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
            trigger = origin.toHermesTrigger()
        ).also { logI("SyncEvent") { it.toString() } }
    )
}

private fun UsageLogCode134.Origin.toHermesTrigger() = when (this) {
    UsageLogCode134.Origin.ACCOUNT_CREATION -> Trigger.ACCOUNT_CREATION
    UsageLogCode134.Origin.CHANGE_MASTER_PASSWORD -> Trigger.CHANGE_MASTER_PASSWORD
    UsageLogCode134.Origin.INITIAL_LOGIN -> Trigger.INITIAL_LOGIN
    UsageLogCode134.Origin.LOGIN -> Trigger.LOGIN
    UsageLogCode134.Origin.MANUAL -> Trigger.MANUAL
    UsageLogCode134.Origin.PERIODIC -> Trigger.PERIODIC
    UsageLogCode134.Origin.PUSH -> Trigger.PUSH
    UsageLogCode134.Origin.SAVE -> Trigger.SAVE
    UsageLogCode134.Origin.SAVE_META -> Trigger.SAVE_META
    UsageLogCode134.Origin.SETTINGS_CHANGE -> Trigger.SETTINGS_CHANGE
    UsageLogCode134.Origin.SHARING -> Trigger.SHARING
    UsageLogCode134.Origin.WAKE -> Trigger.WAKE
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
            val desc = if (causes.anyInstanceOf<DashlaneApiOfflineException>()) ErrorDescription.NO_NETWORK else null
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
        causes.any { it.stackTrace.any { element -> element.className == SyncVaultImplLegacy::class.java.name } } ->
            ErrorName.DATABASE to null
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

data class SyncErrorData(val name: ErrorName, val description: ErrorDescription?, val step: ErrorStep)