package com.dashlane.dagger.singleton

import android.content.Context
import android.os.Build
import com.dashlane.BuildConfig
import com.dashlane.common.logger.DeveloperInfoLogger
import com.dashlane.cryptography.CryptographyAppKeyStore
import com.dashlane.debug.services.DaDaDaHermes
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.AnalyticsIdRepository
import com.dashlane.hermes.AppInfo
import com.dashlane.hermes.LogFlush
import com.dashlane.hermes.LogIdGeneratorImpl
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.OsInfo
import com.dashlane.hermes.TrackingRepository
import com.dashlane.hermes.service.ActivityLogErrorReporter
import com.dashlane.hermes.service.ActivityLogService
import com.dashlane.hermes.service.AnalyticsErrorReporter
import com.dashlane.hermes.service.AnalyticsLogService
import com.dashlane.hermes.service.AnalyticsLogServiceImpl
import com.dashlane.hermes.storage.LogStorage
import com.dashlane.hermes.storage.SecureFileLogStorage
import com.dashlane.logger.ActivityLogErrorReporterImpl
import com.dashlane.logger.AnalyticsErrorReporterImpl
import com.dashlane.logger.utils.HermesDebugUtil
import com.dashlane.network.ServerUrlOverride
import com.dashlane.nitro.Nitro
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.analytics.AnalyticsApi
import com.dashlane.useractivity.InMemoryLogStorage
import com.dashlane.useractivity.TestLogChecker
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.time.Clock
import java.util.Optional
import java.util.UUID
import javax.inject.Provider
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
        testLogChecker: TestLogChecker,
        @IoCoroutineDispatcher ioDispatcher: CoroutineDispatcher,
    ): AnalyticsLogService = AnalyticsLogServiceImpl(analyticsApi, testLogChecker, ioDispatcher)

    @Provides
    open fun provideActivityLogService(
        nitro: Nitro,
        userFeaturesChecker: Provider<UserFeaturesChecker>,
        serverUrlOverride: Optional<ServerUrlOverride>,
        dashlaneApi: DashlaneApi,
        @IoCoroutineDispatcher ioDispatcher: CoroutineDispatcher
    ): ActivityLogService = com.dashlane.analytics.ActivityLogService(
        nitro,
        userFeaturesChecker,
        dashlaneApi.endpoints.teams.storeActivityLogs,
        serverUrlOverride,
        ioDispatcher
    )

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
        dadadaHermes: DaDaDaHermes,
        globalPreferencesManager: GlobalPreferencesManager
    ) = HermesDebugUtil(dadadaHermes, globalPreferencesManager)

    @Provides
    open fun provideActivityLogErrorReporter(developerInfoLogger: DeveloperInfoLogger): ActivityLogErrorReporter =
        ActivityLogErrorReporterImpl()

    @Provides
    open fun provideLogErrorReporter(developerInfoLogger: DeveloperInfoLogger): AnalyticsErrorReporter =
        AnalyticsErrorReporterImpl()

    private fun appInfo() = AppInfo(
        if (BuildConfig.VERSION_NAME == "dev_build_dev") "0-dev_build" else BuildConfig.VERSION_NAME,
        if (BuildConfig.PLAYSTORE_BUILD) AppInfo.Build.PRODUCTION else AppInfo.Build.DEV,
        AppInfo.App.MAIN
    )
}