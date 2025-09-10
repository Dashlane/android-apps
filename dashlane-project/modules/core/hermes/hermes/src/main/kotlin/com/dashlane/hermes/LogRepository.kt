package com.dashlane.hermes

import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.Browse
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Context
import com.dashlane.hermes.generated.definitions.ContextAnonymous
import com.dashlane.hermes.generated.definitions.Device
import com.dashlane.hermes.generated.definitions.Session
import com.dashlane.hermes.generated.definitions.User
import com.dashlane.hermes.service.ActivityLogErrorReporter
import com.dashlane.hermes.service.ActivityLogErrorReporter.InvalidLog
import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.hermes.service.AnalyticsErrorReporter
import com.dashlane.hermes.service.AnalyticsLogService
import com.dashlane.hermes.storage.LogItem
import com.dashlane.hermes.storage.LogStorage
import com.dashlane.hermes.storage.StorageCategory
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.analytics.events.EventService
import com.dashlane.server.api.analytics.events.exceptions.PayloadTooLargeException
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.server.api.time.toInstant
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

interface LogRepository : AnalyticsIdRepository {
    fun sessionStarted(authorization: Authorization.User)

    fun sessionEnded()

    fun queueEvent(event: TrackingLog)

    fun queueEvent(event: ActivityLog)

    fun queuePageView(
        component: BrowseComponent,
        page: AnyPage,
    )

    suspend fun flushLogs(authorization: Authorization.User? = null)
}

class TrackingRepository constructor(
    private val coroutineScope: CoroutineScope,
    private val analyticsLogService: AnalyticsLogService,
    private val activityLogService: ActivityLogService,
    private val logStorage: LogStorage,
    analyticsIdRepository: AnalyticsIdRepository,
    private val appInfo: AppInfo,
    private val osInfo: OsInfo,
    private val logIdGenerator: LogIdGenerator,
    private val logFlush: LogFlush,
    private val clock: Clock,
    private val errorReporter: AnalyticsErrorReporter = defaultAnalyticsErrorReporter(),
    private val activityLogErrorReporter: ActivityLogErrorReporter = defaultActivityLogErrorReporter(),
    private val configurationUtil: ConfigurationUtil = DefaultConfigurationUtil,
) : LogRepository, AnalyticsIdRepository by analyticsIdRepository {
    private val gson = Gson()
    private var currentBatchSize = LOG_BATCH_SIZE
    private var currentActivityLogBatchSize = ACTIVITY_LOG_BATCH_SIZE

    private val context
        get() =
            Context(
                app = appInfo.toAppDefinition(),
                user = getAnalyticsInfo().userId?.let { User(it) },
                device =
                    Device(
                        os = osInfo.toOsDefinition(),
                        installationId = getAnalyticsInfo().installationId,
                        id = getAnalyticsInfo().deviceId,
                    ),
            )

    private val contextAnonymous
        get() =
            ContextAnonymous(
                app = appInfo.toAppDefinition(),
                os = osInfo.toOsDefinition(),
            )

    private var previousComponent: BrowseComponent? = null
    private var previousPage: AnyPage? = null

    private var authorization: Authorization.User? = null

    private val sessionGenerator: SessionGenerator = SessionGenerator(logIdGenerator)

    override fun sessionStarted(authorization: Authorization.User) {
        this.authorization = authorization
    }

    override fun sessionEnded() {
        logFlush.invoke(authorization)
        this.authorization = null
    }

    override fun queueEvent(event: TrackingLog) {
        if (!configurationUtil.allowSendLogs) return
        val log =
            collectContent(
                Browse(originComponent = previousComponent, originPage = previousPage),
                event,
            )
        printLogIfNecessary(log)
        coroutineScope.launch {
            logStorage.storeLog(log)
            if (event.priority || configurationUtil.isAutoFlushEnabled) {
                logFlush.invoke(authorization)
            }
        }
    }

    override fun queueEvent(event: ActivityLog) {
        val log =
            LogItem(
                dateTime = event.dateTime.toInstant(),
                logId = UUID.fromString(event.uuid),
                logContent = gson.toJson(event),
                category = TrackingLog.Category.ACTIVITY_LOG,
            )
        printLogIfNecessary(log)
        coroutineScope.launch {
            logStorage.storeLog(log)
            if (configurationUtil.isAutoFlushEnabled) logFlush.invoke(authorization)
        }
    }

    override fun queuePageView(
        component: BrowseComponent,
        page: AnyPage,
    ) {
        if (!configurationUtil.allowSendLogs) return
        val browse = refreshBrowsingContext(component, page)
        val log = collectContent(browse)
        printLogIfNecessary(log)
        coroutineScope.launch {
            logStorage.storeLog(log)
            if (page.priority || configurationUtil.isAutoFlushEnabled) logFlush.invoke(authorization)
        }
    }

    override suspend fun flushLogs(authorization: Authorization.User?) {
        
        val authorization = authorization ?: this.authorization
        StorageCategory.entries.forEach { category ->
            logStorage.readLogs(category).chunked(currentBatchSize).forEach { logBatch ->
                if (category == StorageCategory.ACTIVITY_LOG) {
                    sendActivityLogBatch(authorization, logBatch)
                } else {
                    sendBatch(authorization?.login, logBatch, category.eventType)
                }
            }
        }
    }

    private suspend fun sendActivityLogBatch(
        authorization: Authorization.User?,
        logBatch: List<LogItem>,
    ) {
        try {
            val activityLogs =
                logBatch.mapNotNull {
                    try {
                        gson.fromJson(it.logContent, ActivityLog::class.java)
                    } catch (e: JsonSyntaxException) {
                        
                        logStorage.deleteLogs(listOf(it))
                        null
                    }
                }
            val invalidActivityLogs =
                authorization?.let {
                    activityLogService.sendLogs(authorization = it, logBatch = activityLogs)
                }

            if (invalidActivityLogs?.isEmpty() == true) {
                logStorage.deleteLogs(logBatch)
                currentActivityLogBatchSize = ACTIVITY_LOG_BATCH_SIZE
            } else {
                
                val invalidLogs =
                    logBatch.mapNotNull { logItem ->
                        val invalidLog =
                            invalidActivityLogs?.find { it.uuid == logItem.logId.toString() }
                                ?: return@mapNotNull null
                        InvalidLog(logItem, invalidLog)
                    }
                logStorage.deleteLogs(invalidLogs.map { it.logItem })
                activityLogErrorReporter.report(authorization?.login, invalidLogs)
            }
        } catch (e: DashlaneApiException) {
            handleErrors(authorization?.login, logBatch, e, isActivityLog = true)
        }
    }

    private suspend fun sendBatch(
        username: String?,
        logBatch: List<LogItem>,
        eventType: EventService.EventType,
    ) {
        try {
            val success =
                analyticsLogService.sendLogs(
                    logBatch.joinToString(separator = "\n") { it.logContent },
                    eventType,
                )
            if (success) {
                logStorage.deleteLogs(logBatch)
                currentBatchSize = LOG_BATCH_SIZE
            } else {
                throw HermesException()
            }
        } catch (e: DashlaneApiException) {
            handleErrors(username, logBatch, e, isActivityLog = false)
        }
    }

    private fun collectContent(
        browse: Browse,
        event: TrackingLog? = null,
    ): LogItem {
        val isPageView = (event == null)
        val clock = clock
        val logId = logIdGenerator.generateUuid()
        val dateTime = ZonedDateTime.now(clock)

        return LogItem(
            dateTime = dateTime.toInstant(),
            logId = logId,
            logContent =
                collectToJsonString { writer, collector ->
                    collector["id"] = logId
                    collector["schema_version"] = TrackingSchemasVersion.VERSION
                    collector["category"] = event?.category?.code ?: TrackingLog.Category.USER.code
                    collector["date"] =
                        if (event?.category == TrackingLog.Category.ANONYMOUS) {
                            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        } else {
                            dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                        }
                    collector["date_origin"] = "local"
                    collector["context"] = getTrackingContext(event?.category)
                    if (event?.category != TrackingLog.Category.ANONYMOUS) {
                        collector["browse"] = browse
                        sessionGenerator.updateSession(clock, isPageView)
                            ?.let { collector["session"] = it }
                    }
                    if (isPageView) {
                        collectPageViewProperties(writer)
                    } else {
                        event?.collectEventProperties(writer)
                    }
                },
            category = event?.category ?: TrackingLog.Category.USER,
        )
    }

    private fun refreshBrowsingContext(
        component: BrowseComponent,
        page: AnyPage,
    ): Browse {
        val originComponent = previousComponent
        val originPage = previousPage

        previousComponent = component
        previousPage = page

        return Browse(
            component = component,
            page = page,
            originComponent = originComponent,
            originPage = originPage,
        )
    }

    private fun getTrackingContext(category: TrackingLog.Category?) =
        when (category) {
            TrackingLog.Category.ANONYMOUS -> contextAnonymous
            else -> context
        }

    private fun printLogIfNecessary(log: LogItem) {
        if (configurationUtil.isVerboseEnabled) {
            println(log)
        }
    }

    private fun TrackingLog.collectEventProperties(writer: JsonWriter) {
        writer.name("properties")
        writer.beginObject()
        this.collect(JsonCollector(writer))
        writer.endObject()
    }

    private fun collectPageViewProperties(writer: JsonWriter) {
        writer.name("properties")
        writer.beginObject()
        writer.name("name")
        writer.value("view_page")
        writer.endObject()
    }

    private fun handleErrors(
        username: String?,
        logBatch: List<LogItem>,
        e: DashlaneApiException,
        isActivityLog: Boolean,
    ) {
        when (e) {
            is PayloadTooLargeException -> {
                
                if (isActivityLog) {
                    currentActivityLogBatchSize = maxOf(1, currentActivityLogBatchSize / 2)
                } else {
                    currentBatchSize = maxOf(1, currentBatchSize / 2)
                }
                val batchSize = if (isActivityLog) currentActivityLogBatchSize else currentBatchSize
                if (batchSize == 1) {
                    
                    errorReporter.report(username, e)
                }
                throw HermesException(cause = e)
            }

            is DashlaneApiIoException -> {
                
            }

            else -> {
                
                errorReporter.report(username, e)
                logStorage.deleteLogs(logBatch)
                if (isActivityLog) {
                    currentActivityLogBatchSize = ACTIVITY_LOG_BATCH_SIZE
                } else {
                    currentBatchSize = LOG_BATCH_SIZE
                }
            }
        }
    }

    internal class SessionGenerator(
        private val logIdGenerator: LogIdGenerator,
    ) {
        private var currentSession: Session? = null
        private var latestActivity: Instant = Instant.EPOCH

        fun updateSession(
            clock: Clock,
            isBrowse: Boolean,
        ): Session? {
            val currentInstant = clock.instant()
            val elapsedTime = ChronoUnit.SECONDS.between(latestActivity, currentInstant)
            return if (isBrowse) {
                updateSessionForBrowse(currentInstant, elapsedTime)
            } else {
                updateSessionForEvent(elapsedTime)
            }
        }

        private fun updateSessionForBrowse(
            currentInstant: Instant,
            elapsedTime: Long,
        ): Session {
            val newSession =
                if (currentSession == null || elapsedTime > SESSION_LIFESPAN_SECONDS) {
                    Session(
                        id = logIdGenerator.generateUuid(),
                        sequenceNumber = 1,
                    )
                } else {
                    currentSession!!.copy(
                        id = currentSession!!.id,
                        sequenceNumber = currentSession!!.sequenceNumber + 1,
                    )
                }
            currentSession = newSession
            latestActivity = currentInstant
            return newSession
        }

        private fun updateSessionForEvent(elapsedTime: Long): Session? {
            val newSession =
                if (currentSession == null || elapsedTime > SESSION_LIFESPAN_SECONDS) {
                    null
                } else {
                    currentSession!!.withIncrementedCount()
                }
            currentSession = newSession
            return newSession
        }

        private fun Session.withIncrementedCount(): Session =
            this.copy(
                id = this.id,
                sequenceNumber = this.sequenceNumber + 1,
            )

        companion object {
            const val SESSION_LIFESPAN_SECONDS = 5 * 60 
        }
    }

    companion object {
        const val LOG_BATCH_SIZE = 450
        const val ACTIVITY_LOG_BATCH_SIZE = 100
    }
}

private fun defaultAnalyticsErrorReporter() =
    object : AnalyticsErrorReporter {
        override fun report(
            username: String?,
            exception: DashlaneApiException,
        ) = Unit
    }

private fun defaultActivityLogErrorReporter() =
    object : ActivityLogErrorReporter {
        override fun report(
            username: String?,
            invalidLogs: List<InvalidLog>,
        ) = Unit
    }