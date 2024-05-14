package com.dashlane.dagger.singleton

import android.app.backup.BackupManager
import android.content.Context
import com.braze.Braze
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.dashlane.BuildConfig
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.DashlaneAnnouncementCenter
import com.dashlane.announcements.modules.BrazeInAppPopupModule
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.accountsmanager.AccountsManager
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.authentication.login.AuthenticationDeviceRepositoryImpl
import com.dashlane.autofill.AutofillAnalyzerDef.IUserPreferencesAccess
import com.dashlane.braze.BrazeInAppMessageSubscriber
import com.dashlane.braze.BrazeWrapper
import com.dashlane.debug.DashlaneBuildConfig
import com.dashlane.events.AppEvents
import com.dashlane.login.lock.LockManager
import com.dashlane.network.inject.RetrofitModule
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.preference.UserPreferencesManager.UserLoggedIn
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionManagerImpl
import com.dashlane.settings.AutofillUserPreferencesAccess
import com.dashlane.storage.securestorage.SecureStorageManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.EmailSuggestionProviderImpl
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.dagger.UserDataAccessorModule
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.ui.activities.onboarding.HomeActivityIntentCoordinator
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.ui.util.DialogHelper
import com.dashlane.url.assetlinks.UrlDomainAssetLinkService
import com.dashlane.url.icon.UrlDomainIconAndroidRepository
import com.dashlane.url.icon.UrlDomainIconDataStore
import com.dashlane.url.icon.UrlDomainIconDatabase
import com.dashlane.url.icon.UrlDomainIconRepository
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import okhttp3.Call
import java.time.Clock
import javax.inject.Singleton

@Module(includes = [BinderModule::class, UserDataAccessorModule::class, RetrofitModule::class, InAppLoginModule::class, TrackingModule::class])
open class SingletonModule {

    @Provides
    @Singleton
    fun provideEmailSuggestionProvider(
        genericDataQuery: GenericDataQuery
    ): EmailSuggestionProvider {
        return EmailSuggestionProviderImpl(genericDataQuery)
    }

    @Provides
    @Singleton
    open fun provideDialogHelper(): DialogHelper {
        return DialogHelper()
    }

    @Provides
    @Singleton
    fun provideAccountsManager(@ApplicationContext context: Context): AccountsManager {
        return AccountsManager(context)
    }

    @Provides
    fun provideUrlDomainAssetLinkService(callFactory: Call.Factory): UrlDomainAssetLinkService {
        return UrlDomainAssetLinkService(callFactory)
    }

    @Singleton
    @Provides
    open fun provideGlobalPreferencesManager(
        @ApplicationContext context: Context
    ): GlobalPreferencesManager {
        return GlobalPreferencesManager(context, BackupManager(context))
    }

    @Singleton
    @Provides
    open fun provideUserPreferencesManager(
        @ApplicationContext context: Context,
        sessionManager: SessionManager
    ): UserPreferencesManager {
        return UserLoggedIn(context, sessionManager)
    }

    @Provides
    fun provideBrazeContentCardSubscriber(
        @ApplicationCoroutineScope coroutineScope: CoroutineScope,
        announcementCenter: AnnouncementCenter,
        lockManager: LockManager
    ): BrazeInAppMessageSubscriber {
        return BrazeInAppPopupModule(coroutineScope, announcementCenter, lockManager)
    }

    @Singleton
    @Provides
    open fun provideBrazeWrapper(
        @ApplicationContext context: Context,
        brazeInAppPopupModule: BrazeInAppMessageSubscriber
    ): BrazeWrapper {
        return BrazeWrapper(
            Braze.getInstance(context),
            BrazeInAppMessageManager.getInstance(),
            brazeInAppPopupModule
        )
    }

    @Provides
    @Singleton
    fun provideAnnouncementCenter(): AnnouncementCenter {
        return DashlaneAnnouncementCenter()
    }

    @Provides
    @Singleton
    fun provideUserSecureDataStorageManager(
        secureDataStoreManager: SecureStorageManager
    ): UserSecureStorageManager {
        return UserSecureStorageManager(secureDataStoreManager)
    }

    @Provides
    @Singleton
    fun provideAccessibleOffersCache(
        service: StoreOffersService,
        sessionManager: SessionManager
    ): StoreOffersCache {
        return StoreOffersCache(service, sessionManager)
    }

    @Provides
    @Singleton
    fun provideCryptoObjectHelper(userPreferencesManager: UserPreferencesManager): CryptoObjectHelper {
        return CryptoObjectHelper(userPreferencesManager)
    }

    @Provides
    @Singleton
    fun provideSessionInitializer(sessionManager: SessionManagerImpl): SessionInitializer {
        return sessionManager
    }

    @Provides
    fun provideUrlDomainIconAndroidRepository(
        iconRepository: UrlDomainIconRepository,
        @ApplicationContext context: Context
    ): UrlDomainIconAndroidRepository {
        return UrlDomainIconAndroidRepository(iconRepository, context)
    }

    @Singleton
    @Provides
    fun provideUrlDomainIconRepository(
        @ApplicationCoroutineScope applicationCoroutineScope: CoroutineScope,
        dataStore: UrlDomainIconDataStore,
        dashlaneApi: DashlaneApi
    ): UrlDomainIconRepository {
        return UrlDomainIconRepository(
            applicationCoroutineScope,
            dataStore,
            dashlaneApi.endpoints.iconcrawler.iconService,
            dashlaneApi.dashlaneTime
        )
    }

    @Singleton
    @Provides
    fun provideUrlDomainIconDataStore(database: UrlDomainIconDatabase): UrlDomainIconDataStore {
        return UrlDomainIconDataStore(database)
    }

    @Singleton
    @Provides
    fun provideUrlDomainIconRoomDatabase(@ApplicationContext context: Context): UrlDomainIconDatabase {
        return UrlDomainIconDatabase.invoke(context)
    }

    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    fun provideDashlaneBuildConfig(): DashlaneBuildConfig {
        return DashlaneBuildConfig(BuildConfig.PLAYSTORE_BUILD, BuildConfig.NIGHTLY_BUILD)
    }

    @Provides
    fun providePostAccountCreationCoordinator(
        @ApplicationContext context: Context,
        appEvents: AppEvents
    ): PostAccountCreationCoordinator {
        return HomeActivityIntentCoordinator(context, appEvents)
    }

    @Provides
    fun provideEmailRepository(
        userStorage: UserStorage,
        authLoginService: AuthLoginService
    ): AuthenticationDeviceRepository {
        return AuthenticationDeviceRepositoryImpl(
            userStorage,
            authLoginService
        )
    }

    @Provides
    fun provideAutofillUserPreferencesAccess(
        mUserPreferencesManager: UserPreferencesManager
    ): IUserPreferencesAccess {
        return AutofillUserPreferencesAccess(mUserPreferencesManager)
    }

    @Provides
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }
}