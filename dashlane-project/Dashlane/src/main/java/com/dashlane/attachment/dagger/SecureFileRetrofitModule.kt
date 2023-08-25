package com.dashlane.attachment.dagger

import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.dashlane.network.inject.LegacyWebservicesApi
import com.dashlane.securefile.services.GetUploadLinkService
import com.dashlane.securefile.services.CommitService
import com.dashlane.securefile.services.GetDownloadLinkService
import com.dashlane.securefile.services.DeleteService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object SecureFileRetrofitModule {
    @Provides
    fun provideGetUploadLinkService(@LegacyWebservicesApi retrofit: Retrofit): GetUploadLinkService =
        retrofit.create()

    @Provides
    fun provideCommitService(@LegacyWebservicesApi retrofit: Retrofit): CommitService =
        retrofit.create()

    @Provides
    fun provideGetDownloadLinkService(@LegacyWebservicesApi retrofit: Retrofit): GetDownloadLinkService =
        retrofit.create()

    @Provides
    fun provideGetDeleteFileService(@LegacyWebservicesApi retrofit: Retrofit): DeleteService =
        retrofit.create()
}