package com.dashlane.dagger.singleton

import android.content.Context
import com.braze.Braze
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.dashlane.BuildConfig
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.DashlaneAnnouncementCenter
import com.dashlane.announcements.modules.BrazeInAppPopupModule
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.login.AuthenticationDeviceRepository
import com.dashlane.authentication.login.AuthenticationDeviceRepositoryImpl
import com.dashlane.braze.BrazeInAppMessageSubscriber
import com.dashlane.braze.BrazeWrapper
import com.dashlane.debug.DashlaneBuildConfig
import com.dashlane.hardwaresecurity.CryptoObjectHelper
import com.dashlane.lock.LockManager
import com.dashlane.network.inject.RetrofitModule
import com.dashlane.preference.PreferencesManager
import com.dashlane.premium.StoreOffersCache
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.payments.StoreOffersService
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionManagerImpl
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.storage.userdata.EmailSuggestionProviderImpl
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.dagger.UserDataAccessorModule
import com.dashlane.ui.PostAccountCreationCoordinator
import com.dashlane.ui.activities.onboarding.HomeActivityIntentCoordinator
import com.dashlane.url.assetlinks.UrlDomainAssetLinkService
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.Call
import java.time.Clock
import javax.inject.Singleton

@Module(includes = [BinderModule::class, UserDataAccessorModule::class, RetrofitModule::class, InAppLoginModule::class, TrackingModule::class])
@InstallIn(SingletonComponent::class)
open class SingletonModule {

    @Provides
    @Singleton
    fun provideEmailSuggestionProvider(
        genericDataQuery: GenericDataQuery
    ): EmailSuggestionProvider {
        return EmailSuggestionProviderImpl(genericDataQuery)
    }

    @Provides
    fun provideUrlDomainAssetLinkService(callFactory: Call.Factory): UrlDomainAssetLinkService {
        return UrlDomainAssetLinkService(callFactory)
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
    fun provideAccessibleOffersCache(
        service: StoreOffersService,
        sessionManager: SessionManager
    ): StoreOffersCache {
        return StoreOffersCache(service, sessionManager)
    }

    @Provides
    @Singleton
    fun provideCryptoObjectHelper(preferencesManager: PreferencesManager): CryptoObjectHelper {
        return CryptoObjectHelper(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideSessionInitializer(sessionManager: SessionManagerImpl): SessionInitializer {
        return sessionManager
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
    ): PostAccountCreationCoordinator {
        return HomeActivityIntentCoordinator(context)
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
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }
}