package com.dashlane.dagger.singleton;

import android.content.Context;
import android.os.Build;

import com.dashlane.BuildConfig;
import com.dashlane.cryptography.CryptographyAppKeyStore;
import com.dashlane.cryptography.CryptographyAppKeyStoreKt;
import com.dashlane.debug.DaDaDa;
import com.dashlane.hermes.AnalyticsIdRepositoryKt;
import com.dashlane.hermes.AppInfo;
import com.dashlane.hermes.LogFlush;
import com.dashlane.hermes.LogFlushFactory;
import com.dashlane.hermes.LogIdGeneratorImpl;
import com.dashlane.hermes.LogRepository;
import com.dashlane.hermes.OsInfo;
import com.dashlane.hermes.TrackingRepository;
import com.dashlane.hermes.service.AnalyticsLogService;
import com.dashlane.hermes.storage.LogStorage;
import com.dashlane.hermes.storage.SecureFileLogStorage;
import com.dashlane.logger.utils.HermesDebugUtil;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.server.api.analytics.AnalyticsApi;
import com.dashlane.useractivity.InMemoryLogStorage;
import com.dashlane.useractivity.TestLogChecker;

import java.io.File;
import java.time.Clock;
import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import kotlinx.coroutines.GlobalScope;

@Module
@InstallIn(SingletonComponent.class)
public class TrackingModule {
    @Provides
    @Singleton
    public LogRepository provideLogRepository(
            @ApplicationContext Context context,
            GlobalPreferencesManager globalPreferencesManager,
            AnalyticsApi analyticsApi,
            TestLogChecker testLogChecker,
            LogFlush logFlush,
            DaDaDa dadada
    ) {
        LogStorage logStorage = getLogStorage(context);
        return new TrackingRepository(
                GlobalScope.INSTANCE,
                new AnalyticsLogService(analyticsApi, testLogChecker),
                logStorage,
                AnalyticsIdRepositoryKt
                        .AnalyticsIdRepository(UUID.fromString(globalPreferencesManager.getInstallationTrackingId())),
                new AppInfo(
                        (BuildConfig.VERSION_NAME.equals("dev_build_dev")) ? "0-dev_build" :
                        BuildConfig.VERSION_NAME,
                        (BuildConfig.PLAYSTORE_BUILD) ? AppInfo.Build.PRODUCTION :
                        AppInfo.Build.DEV,
                        AppInfo.App.MAIN
                ),
                new OsInfo(
                        context.getResources().getConfiguration().getLocales().get(0).toString().replace("_", "-"),
                        Build.VERSION.RELEASE
                ),
                new LogIdGeneratorImpl(),
                logFlush,
                Clock.systemUTC(),
                e -> {
                    
                },
                new HermesDebugUtil(dadada, globalPreferencesManager)
        );
    }

    private static LogStorage getLogStorage(Context context) {

        File logDirectory = context.getFilesDir();
        File keyStoreDirectory = new File(context.getDataDir(), "keyStore");
        if (!keyStoreDirectory.mkdirs() && !keyStoreDirectory.exists()) {
            
            return new InMemoryLogStorage();
        }
        CryptographyAppKeyStore keyStore =
                CryptographyAppKeyStoreKt.CryptographyAppKeyStore(context, keyStoreDirectory);
        return new SecureFileLogStorage(logDirectory, keyStore);
    }

    @Provides
    public LogFlush provideLogFlush(@ApplicationContext Context context) {
        return LogFlushFactory.create(context);
    }
}
