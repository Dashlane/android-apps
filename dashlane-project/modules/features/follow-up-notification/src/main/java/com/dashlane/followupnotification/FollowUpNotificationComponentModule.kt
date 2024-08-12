package com.dashlane.followupnotification

import com.dashlane.followupnotification.api.FollowUpNotificationApi
import com.dashlane.followupnotification.api.FollowUpNotificationApiImpl
import com.dashlane.followupnotification.api.FollowUpNotificationApiNoOperationImpl
import com.dashlane.followupnotification.api.FollowUpNotificationApiProvider
import com.dashlane.followupnotification.api.FollowUpNotificationApiProviderImpl
import com.dashlane.followupnotification.data.FollowUpNotificationRepository
import com.dashlane.followupnotification.data.FollowUpNotificationRepositoryMemoryImpl
import com.dashlane.followupnotification.domain.CopyFollowUpNotificationToClipboard
import com.dashlane.followupnotification.domain.CopyFollowUpNotificationToClipboardImpl
import com.dashlane.followupnotification.domain.CreateFollowUpNotification
import com.dashlane.followupnotification.domain.CreateFollowUpNotificationImpl
import com.dashlane.followupnotification.domain.FollowUpNotificationSettings
import com.dashlane.followupnotification.domain.FollowUpNotificationSettingsImpl
import com.dashlane.followupnotification.services.FollowUpNotificationDiscoveryService
import com.dashlane.followupnotification.services.FollowUpNotificationDiscoveryServiceImpl
import com.dashlane.followupnotification.services.FollowUpNotificationDisplayService
import com.dashlane.followupnotification.services.FollowUpNotificationDisplayServiceImpl
import com.dashlane.followupnotification.services.FollowUpNotificationDynamicData
import com.dashlane.followupnotification.services.FollowUpNotificationDynamicDataImpl
import com.dashlane.followupnotification.services.FollowUpNotificationFlags
import com.dashlane.followupnotification.services.FollowUpNotificationFlagsImpl
import com.dashlane.followupnotification.services.FollowUpNotificationsStrings
import com.dashlane.followupnotification.services.FollowUpNotificationsStringsImpl
import com.dashlane.followupnotification.services.VaultItemContentService
import com.dashlane.followupnotification.services.VaultItemContentServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FollowUpNotificationComponentModule {
    @Provides
    fun providesFollowUpNotificationFlags(impl: FollowUpNotificationFlagsImpl):
            FollowUpNotificationFlags = impl

    @Provides
    fun providesFollowUpNotificationsStrings(impl: FollowUpNotificationsStringsImpl):
            FollowUpNotificationsStrings = impl

    @Provides
    @Singleton
    fun providesFollowUpNotificationRepository(impl: FollowUpNotificationRepositoryMemoryImpl):
            FollowUpNotificationRepository = impl

    @Provides
    fun providesCreateFollowUpNotification(impl: CreateFollowUpNotificationImpl): CreateFollowUpNotification = impl

    @Provides
    fun providesCopyFollowUpNotificationToClipboard(impl: CopyFollowUpNotificationToClipboardImpl): CopyFollowUpNotificationToClipboard =
        impl

    @Provides
    fun providesFollowUpNotificationSettings(impl: FollowUpNotificationSettingsImpl): FollowUpNotificationSettings =
        impl

    @Provides
    @Singleton
    fun providesFollowUpNotificationDisplayService(impl: FollowUpNotificationDisplayServiceImpl):
            FollowUpNotificationDisplayService = impl

    @Provides
    fun providesFollowUpNotificationApiProvider(impl: FollowUpNotificationApiProviderImpl):
            FollowUpNotificationApiProvider = impl

    @Provides
    @Named("activeFollowUp")
    fun providesFollowUpNotificationApiImpl(impl: FollowUpNotificationApiImpl): FollowUpNotificationApi = impl

    @Provides
    @Named("noOperationFollowUp")
    fun providesFollowUpNotificationApiNoOperationImpl(impl: FollowUpNotificationApiNoOperationImpl):
            FollowUpNotificationApi = impl

    @Provides
    fun providesFollowUpNotificationDynamicData(impl: FollowUpNotificationDynamicDataImpl):
            FollowUpNotificationDynamicData = impl

    @Provides
    fun providesVaultItemContentDisplayService(impl: VaultItemContentServiceImpl):
            VaultItemContentService = impl

    @Provides
    fun providesFollowUpNotificationDiscoveryService(impl: FollowUpNotificationDiscoveryServiceImpl):
            FollowUpNotificationDiscoveryService = impl
}