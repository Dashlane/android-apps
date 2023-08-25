package com.dashlane.network.inject

import com.dashlane.network.webservices.CrashReportUploadService
import com.dashlane.network.webservices.DownloadFileService
import com.dashlane.network.webservices.GetCountryService
import com.dashlane.network.webservices.GetDevicesNumberService
import com.dashlane.network.webservices.GetSharingLinkService
import com.dashlane.network.webservices.SpaceDeletedService
import com.dashlane.network.webservices.UploadFileService
import com.dashlane.network.webservices.VerifyReceiptService
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.network.webservices.authentication.OtpPhoneLostService
import com.dashlane.network.webservices.fcm.PushNotificationService
import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create

@Module(includes = [DashlaneApiEndpointsModule::class])
class DashlaneServicesModule {

    @Provides
    fun getGetCountryService(@LegacyWebservicesApi retrofit: Retrofit): GetCountryService = retrofit.create()

    @Provides
    fun getPushNotificationService(@LegacyWebservicesApi retrofit: Retrofit): PushNotificationService =
        retrofit.create()

    @Provides
    fun getGetDevicesNumberService(@LegacyWebservicesApi retrofit: Retrofit): GetDevicesNumberService =
        retrofit.create()

    @Provides
    fun getVerifyReceiptService(@LegacyWebservicesApi retrofit: Retrofit): VerifyReceiptService = retrofit.create()

    @Provides
    fun getCrashReportUploadService(@LegacyWebservicesApi retrofit: Retrofit): CrashReportUploadService =
        retrofit.create()

    @Provides
    fun getDownloadFileService(@Streaming retrofit: Retrofit): DownloadFileService {
        
        
        
        return retrofit.create<DownloadFileService>(DownloadFileService::class.java)
    }

    @Provides
    fun getUploadFileService(@Streaming retrofit: Retrofit): UploadFileService {
        
        
        
        return retrofit.create(UploadFileService::class.java)
    }

    @Provides
    fun getSpaceDeletedService(@LegacyWebservicesApi retrofit: Retrofit): SpaceDeletedService = retrofit.create()

    @Provides
    fun getGetTokenService(@LegacyWebservicesApi retrofit: Retrofit): GetTokenService = retrofit.create()

    @Provides
    fun getOtpPhoneLostService(@LegacyWebservicesApi retrofit: Retrofit): OtpPhoneLostService = retrofit.create()

    @Provides
    fun getGetSharingLinkService(@LegacyWebservicesApi retrofit: Retrofit): GetSharingLinkService = retrofit.create()
}
