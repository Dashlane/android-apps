package com.dashlane.dagger.singleton

import android.content.Context
import android.os.Build
import com.dashlane.BuildConfig
import com.dashlane.cryptography.CryptographyAppKeyStore
import com.dashlane.debug.DaDaDa
import com.dashlane.hermes.AnalyticsIdRepository
import com.dashlane.hermes.AppInfo
import com.dashlane.hermes.LogFlush
import com.dashlane.hermes.LogIdGeneratorImpl
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.OsInfo
import com.dashlane.hermes.TrackingRepository
import com.dashlane.hermes.service.ActivityLogErrorReporter
import com.dashlane.hermes.service.ActivityLogErrorReporter.InvalidActivityLog
import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.hermes.service.AnalyticsErrorReporter
import com.dashlane.hermes.service.AnalyticsLogService
import com.dashlane.hermes.storage.LogStorage
import com.dashlane.hermes.storage.SecureFileLogStorage
import com.dashlane.logger.utils.HermesDebugUtil
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.analytics.AnalyticsApi
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.useractivity.DeveloperInfoLogger
import com.dashlane.useractivity.InMemoryLogStorage
import com.dashlane.useractivity.TestLogChecker
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.stackTraceToSafeString
import com.dashlane.util.tryOrNull
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.time.Clock
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class TrackingModule {
    @Provides
    @Singleton
    open fun provideLogRepository(
        @ApplicationCoroutineScope applicationCoroutineScope: CoroutineScope,
        analyticsIdRepository: AnalyticsIdRepository,
        logStorage: LogStorage,
        analyticsLogService: AnalyticsLogService,
        activityLogService: ActivityLogService,
        osInfo: OsInfo,
        logFlush: LogFlush,
        hermesDebugUtil: HermesDebugUtil,
        logErrorReporter: AnalyticsErrorReporter,
        activityLogErrorReporter: ActivityLogErrorReporter
    ): LogRepository = TrackingRepository(
        coroutineScope = applicationCoroutineScope,
        analyticsLogService = analyticsLogService,
        activityLogService = activityLogService,
        logStorage = logStorage,
        analyticsIdRepository = analyticsIdRepository,
        appInfo = appInfo(),
        osInfo = osInfo,
        logIdGenerator = LogIdGeneratorImpl(),
        logFlush = logFlush,
        clock = Clock.systemUTC(),
        configurationUtil = hermesDebugUtil,
        errorReporter = logErrorReporter,
        activityLogErrorReporter = activityLogErrorReporter
    )

    @Provides
    open fun provideLogFlush(@ApplicationContext context: Context) = LogFlush(context)

    @Provides
    @Singleton
    open fun provideLogStorage(@ApplicationContext context: Context): LogStorage {
        val logDirectory = context.filesDir
        val keyStoreDirectory = File(context.dataDir, "keyStore")
        if (!keyStoreDirectory.mkdirs() && !keyStoreDirectory.exists()) {
            
            return InMemoryLogStorage()
        }
        val keyStore = CryptographyAppKeyStore(context, keyStoreDirectory)
        return SecureFileLogStorage(logDirectory, keyStore)
    }

    @Provides
    open fun provideAnalyticsLogService(
        analyticsApi: AnalyticsApi,
        testLogChecker: TestLogChecker
    ) = AnalyticsLogService(analyticsApi, testLogChecker)

    @Provides
    open fun provideActivityLogService(dashlaneApi: DashlaneApi) =
        object : ActivityLogService(dashlaneApi.endpoints.teams.storeActivityLogs) {
            override val authorization: Authorization.User?
                get() = SingletonProvider.getSessionManager().session?.let {
                    Authorization.User(it.userId, it.accessKey, it.secretKey)
                }
        }

    @Provides
    open fun provideAnalyticsIdRepository(globalPreferencesManager: GlobalPreferencesManager) =
        AnalyticsIdRepository(UUID.fromString(globalPreferencesManager.installationTrackingId))

    @Provides
    open fun provideOSInfo(@ApplicationContext context: Context) = OsInfo(
        context.resources.configuration.locales[0].toString().replace("_", "-"),
        Build.VERSION.RELEASE
    )

    @Provides
    open fun provideHermesDebugUtil(
        dadada: DaDaDa,
        globalPreferencesManager: GlobalPreferencesManager
    ) = HermesDebugUtil(dadada, globalPreferencesManager)

    @Provides
    open fun provideActivityLogErrorReporter(developerInfoLogger: DeveloperInfoLogger) =
        object : ActivityLogErrorReporter {
            override fun report(invalidLogs: List<InvalidActivityLog>) {
                SingletonProvider.getSessionManager().session?.let { session ->
                    invalidLogs.forEach { log ->
                        val error = log.invalidLog.error.key
                        val activityLog = tryOrNull {
                            Gson().fromJson(log.logItem.logContent, ActivityLog::class.java)
                        }
                        developerInfoLogger.log(
                            session = session,
                            action = "StoreActivityLog",
                            message = "$error - An activity log with a log type of " +
                                "${activityLog?.logType?.key} and uid: ${log.logItem.logId} has " +
                                "been detected to have a technical / schema error",
                            exceptionType = error
                        )
                    }
                }
            }
        }

    @Provides
    open fun provideLogErrorReporter(developerInfoLogger: DeveloperInfoLogger) =
        object : AnalyticsErrorReporter {
            override fun report(exception: DashlaneApiException) {
                SingletonProvider.getSessionManager().session?.let { session ->
                    developerInfoLogger.log(
                        session = session,
                        action = "Hermes",
                        message = exception.message,
                        exceptionType = exception.stackTraceToSafeString()
                    )
                }
            }
        }

    fun appInfo() = AppInfo(
        if (BuildConfig.VERSION_NAME == "dev_build_dev") "0-dev_build" else BuildConfig.VERSION_NAME,
        if (BuildConfig.PLAYSTORE_BUILD) AppInfo.Build.PRODUCTION else AppInfo.Build.DEV,
        AppInfo.App.MAIN
    )
}