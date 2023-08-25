package com.dashlane.dagger.singleton;

import android.content.Context;

import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.session.BySessionRepository;
import com.dashlane.session.repository.BySessionUsageLogRepository;
import com.dashlane.session.repository.UserDataRepository;
import com.dashlane.useractivity.AggregateUserActivity;
import com.dashlane.useractivity.log.DeviceExtraData;
import com.dashlane.useractivity.log.UserActivityDataStore;
import com.dashlane.useractivity.log.UserActivityDataStoreCompat;
import com.dashlane.useractivity.log.UserActivityDataStoreFactory;
import com.dashlane.useractivity.log.UserActivityDataStoreLegacy;
import com.dashlane.useractivity.log.UserActivityDatabase;
import com.dashlane.useractivity.log.UserActivityFlush;
import com.dashlane.useractivity.log.UserActivityFlushFactory;
import com.dashlane.useractivity.log.UserActivityRepository;
import com.dashlane.useractivity.log.UserActivityRepositoryFactory;
import com.dashlane.useractivity.log.install.InstallLogRepository;
import com.dashlane.useractivity.log.install.InstallLogRepositoryFactory;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope;

import java.time.Clock;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.CoroutineScope;
import okhttp3.Call;

@Module
public class UserActivityModule {

    @Provides
    InstallLogRepository provideInstallLogRepository(
        @ApplicationCoroutineScope CoroutineScope applicationCoroutineScope,
        UserActivityFlush userActivityFlush,
        UserActivityDataStore userActivityDataStore,
        DeviceExtraData deviceExtraData
    ) {
        return InstallLogRepositoryFactory.create(
            applicationCoroutineScope,
            userActivityDataStore,
            deviceExtraData,
            userActivityFlush,
            Clock.systemDefaultZone());
    }

    @Provides
    @Singleton
    BySessionRepository<UsageLogRepository> provideBySessionUsageLogRepository(
        @ApplicationContext Context context,
        UserDataRepository userDataRepository,
        Lazy<AggregateUserActivity> aggregateUserActivityLazy) {
        return new BySessionUsageLogRepository(
            context,
            userDataRepository,
            aggregateUserActivityLazy
        );
    }

    @Provides
    UserActivityFlush provideUserActivityFlush(@ApplicationContext Context context) {
        return UserActivityFlushFactory.create(context);
    }

    @Provides
    @Singleton
    UserActivityRepository getUserActivityRepository(
        UserActivityDataStore userActivityDataStore,
        Call.Factory callFactory
    ) {
        return UserActivityRepositoryFactory.create(userActivityDataStore, callFactory);
    }

    @Provides
    @Singleton
    UserActivityDataStore provideDataStore(
        @ApplicationContext
        Context context,
        GlobalPreferencesManager globalPreferencesManager,
        UserPreferencesManager userPreferencesManager
    ) {
        return new UserActivityDataStoreCompat(
            globalPreferencesManager,
            UserActivityDataStoreFactory.create(
                UserActivityDatabase.create(context)
            ),
            new UserActivityDataStoreLegacy(
                globalPreferencesManager,
                userPreferencesManager
            )
        );
    }
}
