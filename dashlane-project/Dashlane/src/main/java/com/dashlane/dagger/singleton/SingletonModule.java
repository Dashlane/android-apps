package com.dashlane.dagger.singleton;

import android.app.backup.BackupManager;
import android.content.Context;

import com.braze.Braze;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.dashlane.BuildConfig;
import com.dashlane.announcements.AnnouncementCenter;
import com.dashlane.announcements.DashlaneAnnouncementCenter;
import com.dashlane.announcements.modules.BrazeInAppPopupModule;
import com.dashlane.authentication.UserStorage;
import com.dashlane.authentication.accountsmanager.AccountsManager;
import com.dashlane.authentication.login.AuthenticationDeviceRepository;
import com.dashlane.authentication.login.AuthenticationDeviceRepositoryImpl;
import com.dashlane.autofill.AutofillAnalyzerDef;
import com.dashlane.braze.BrazeInAppMessageSubscriber;
import com.dashlane.braze.BrazeWrapper;
import com.dashlane.debug.DashlaneBuildConfig;
import com.dashlane.device.DeviceInfoRepository;
import com.dashlane.device.DeviceService;
import com.dashlane.featureflipping.FeatureFlipManager;
import com.dashlane.network.inject.LegacyWebservicesApi;
import com.dashlane.network.inject.RetrofitModule;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.server.api.DashlaneApi;
import com.dashlane.server.api.endpoints.authentication.AuthLoginService;
import com.dashlane.server.api.endpoints.payments.StoreOffersService;
import com.dashlane.session.BySessionRepository;
import com.dashlane.session.Session;
import com.dashlane.session.SessionInitializer;
import com.dashlane.session.SessionManager;
import com.dashlane.session.SessionManagerImpl;
import com.dashlane.session.repository.AccountStatusRepository;
import com.dashlane.settings.AutofillUserPreferencesAccess;
import com.dashlane.storage.securestorage.SecureStorageManager;
import com.dashlane.storage.securestorage.UserSecureStorageManager;
import com.dashlane.storage.userdata.EmailSuggestionProvider;
import com.dashlane.storage.userdata.EmailSuggestionProviderImpl;
import com.dashlane.storage.userdata.accessor.MainDataAccessor;
import com.dashlane.storage.userdata.accessor.dagger.UserDataAccessorModule;
import com.dashlane.teamspaces.manager.TeamspaceAccessor;
import com.dashlane.teamspaces.manager.TeamspaceAccessorProvider;
import com.dashlane.ui.PostAccountCreationCoordinator;
import com.dashlane.ui.activities.onboarding.HomeActivityIntentCoordinator;
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache;
import com.dashlane.ui.util.DialogHelper;
import com.dashlane.ui.widgets.Notificator;
import com.dashlane.url.assetlinks.UrlDomainAssetLinkService;
import com.dashlane.url.assetlinks.UrlDomainAssetLinkServiceKt;
import com.dashlane.url.icon.UrlDomainIconAndroidRepository;
import com.dashlane.url.icon.UrlDomainIconAndroidRepositoryKt;
import com.dashlane.url.icon.UrlDomainIconDataStore;
import com.dashlane.url.icon.UrlDomainIconDatabase;
import com.dashlane.url.icon.UrlDomainIconDatabaseKt;
import com.dashlane.url.icon.UrlDomainIconRepository;
import com.dashlane.url.icon.UrlDomainIconRepositoryUtils;
import com.dashlane.useractivity.LogSenderService;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.userfeatures.UserFeaturesCheckerImpl;
import com.dashlane.usersupportreporter.UserSupportFileLogger;
import com.dashlane.util.ThreadHelper;
import com.dashlane.util.hardwaresecurity.CryptoObjectHelper;
import com.dashlane.util.inject.OptionalProvider;
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope;
import com.dashlane.util.userfeatures.UserFeaturesChecker;
import com.google.gson.Gson;

import java.time.Clock;

import javax.inject.Singleton;

import androidx.annotation.Nullable;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
import okhttp3.Call;
import retrofit2.Retrofit;



@Module(includes = {BinderModule.class, UserDataAccessorModule.class, RetrofitModule.class,
        InAppLoginModule.class, TrackingModule.class})
public class SingletonModule {

    @Provides
    
    public OptionalProvider<TeamspaceAccessor> provideTeamspaceAccessor(
            TeamspaceAccessorProvider provider) {
        return provider;
    }

    @Provides
    @Singleton
    public EmailSuggestionProvider provideEmailSuggestionProvider(
            MainDataAccessor mainDataAccessor) {
        return new EmailSuggestionProviderImpl(mainDataAccessor);
    }

    @Provides
    @Singleton
    public DialogHelper provideDialogHelper() {
        return new DialogHelper();
    }

    @Provides
    @Singleton
    public AccountsManager provideAccountsManager(@ApplicationContext Context context) {
        return new AccountsManager(context);
    }

    @Provides
    public UrlDomainAssetLinkService provideUrlDomainAssetLinkService(Call.Factory callFactory) {
        return UrlDomainAssetLinkServiceKt.UrlDomainAssetLinkService(callFactory);
    }

    @Provides
    @Singleton
    public ThreadHelper provideThreadHelper() {
        return new ThreadHelper();
    }

    @Provides
    @Singleton
    public UserFeaturesChecker provideUserFeatureChecker(SessionManager sessionManager,
                                                         AccountStatusRepository accountStatusRepository,
                                                         FeatureFlipManager userFeature) {
        return new UserFeaturesCheckerImpl(sessionManager, accountStatusRepository, userFeature);
    }

    @Singleton
    @Provides
    public GlobalPreferencesManager provideGlobalPreferencesManager(
            @ApplicationContext Context context) {
        return new GlobalPreferencesManager(context, new BackupManager(context));
    }

    @Singleton
    @Provides
    public UserPreferencesManager provideUserPreferencesManager(@ApplicationContext Context context,
                                                                SessionManager sessionManager) {
        return new UserPreferencesManager.UserLoggedIn(context, sessionManager);
    }

    @Provides
    public BrazeInAppMessageSubscriber provideBrazeContentCardSubscriber(
            @GlobalCoroutineScope CoroutineScope coroutineScope,
            AnnouncementCenter announcementCenter,
            UserFeaturesChecker featuresChecker
    ) {
        return new BrazeInAppPopupModule(coroutineScope, announcementCenter, featuresChecker);
    }

    @Singleton
    @Provides
    public BrazeWrapper provideBrazeWrapper(@ApplicationContext Context context, BrazeInAppMessageSubscriber brazeInAppPopupModule) {
        return new BrazeWrapper(Braze.getInstance(context), BrazeInAppMessageManager.getInstance(), brazeInAppPopupModule);
    }

    @Provides
    @Singleton
    public Notificator provideNotificator() {
        return new Notificator();
    }

    @Provides
    @Singleton
    public AnnouncementCenter provideAnnouncementCenter() {
        return new DashlaneAnnouncementCenter();
    }

    @Provides
    @Singleton
    public UserSecureStorageManager provideUserSecureDataStorageManager(
            SecureStorageManager secureDataStoreManager
    ) {
        return new UserSecureStorageManager(secureDataStoreManager);
    }

    
    @Nullable
    @Provides
    public UsageLogRepository provideCurrentSessionUsageLogRepository(
            SessionManager sessionManager,
            BySessionRepository<UsageLogRepository> bySessionRepositoryUsageLogRepository) {
        Session session = sessionManager.getSession();
        if (session == null) return null;
        return bySessionRepositoryUsageLogRepository.get(session);
    }

    @Provides
    @Singleton
    public StoreOffersCache provideAccessibleOffersCache(StoreOffersService service,
                                                         AccountStatusRepository accountStatusRepository,
                                                         SessionManager sessionManager,
                                                         BySessionRepository<UsageLogRepository> bySessionUsageLogRepository,
                                                         DeviceInfoRepository deviceInfoRepository) {
        return new StoreOffersCache(service,
                accountStatusRepository,
                sessionManager,
                bySessionUsageLogRepository,
                deviceInfoRepository);
    }

    @Provides
    @Singleton
    public LogSenderService provideLogSenderService(@LegacyWebservicesApi Retrofit retrofit) {
        return retrofit.create(LogSenderService.class);
    }

    @Provides
    @Singleton
    public DeviceService provideDeviceService(@LegacyWebservicesApi Retrofit retrofit) {
        return retrofit.create(DeviceService.class);
    }

    @Provides
    @Singleton
    public CryptoObjectHelper provideCryptoObjectHelper(
            UserSupportFileLogger userSupportFileLogger,
            UserPreferencesManager userPreferencesManager) {
        return new CryptoObjectHelper(userSupportFileLogger, userPreferencesManager);
    }

    @Provides
    @Singleton
    public SessionManager provideSessionManager(SessionManagerImpl sessionManager) {
        return sessionManager;
    }

    @Provides
    @Singleton
    public SessionInitializer provideSessionInitializer(SessionManagerImpl sessionManager) {
        return sessionManager;
    }

    @Provides
    static PostAccountCreationCoordinator providePostAccountCreationCoordinator() {
        return HomeActivityIntentCoordinator.Companion;
    }

    @Provides
    static AuthenticationDeviceRepository provideEmailRepository(
            UserStorage userStorage,
            AuthLoginService authLoginService) {
        return new AuthenticationDeviceRepositoryImpl(
                userStorage,
                authLoginService
        );
    }

    @Provides
    UrlDomainIconAndroidRepository provideUrlDomainIconAndroidRepository(
            UrlDomainIconRepository iconRepository,
            @ApplicationContext Context context) {
        return UrlDomainIconAndroidRepositoryKt.create(iconRepository, context);
    }

    @Singleton
    @Provides
    UrlDomainIconRepository provideUrlDomainIconRepository(UrlDomainIconDataStore dataStore,
                                                           DashlaneApi dashlaneApi) {
        return UrlDomainIconRepositoryUtils.create(
                GlobalScope.INSTANCE,
                dataStore,
                dashlaneApi.getEndpoints().getIconcrawler().getIconService(),
                dashlaneApi.getDashlaneTime()
        );
    }

    @Singleton
    @Provides
    UrlDomainIconDataStore provideUrlDomainIconDataStore(UrlDomainIconDatabase database) {
        return UrlDomainIconDatabaseKt.create(database);
    }

    @Singleton
    @Provides
    UrlDomainIconDatabase provideUrlDomainIconRoomDatabase(@ApplicationContext Context context) {
        return UrlDomainIconDatabase.create(context);
    }

    @Provides
    static AutofillAnalyzerDef.IUserPreferencesAccess provideAutofillUserPreferencesAccess(
            UserPreferencesManager mUserPreferencesManager) {
        return new AutofillUserPreferencesAccess(mUserPreferencesManager);
    }

    @Provides
    @GlobalCoroutineScope
    CoroutineScope provideCoroutineScope() {
        return GlobalScope.INSTANCE;
    }

    @Provides
    static Clock provideClock() {
        return Clock.systemDefaultZone();
    }

    @Provides
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    DashlaneBuildConfig provideDashlaneBuildConfig() {
        return new DashlaneBuildConfig(BuildConfig.CHECK_DADADA_SIGNATURE, BuildConfig.PLAYSTORE_BUILD);
    }
}
