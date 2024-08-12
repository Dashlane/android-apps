package com.dashlane.network.inject

import com.dashlane.network.webservices.DownloadFileService
import com.dashlane.network.webservices.UploadFileService
import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module(includes = [DashlaneApiEndpointsModule::class])
@InstallIn(SingletonComponent::class)
class DashlaneServicesModule {

    @Provides
    fun getDownloadFileService(retrofit: Retrofit): DownloadFileService {
        
        
        
        return retrofit.create<DownloadFileService>(DownloadFileService::class.java)
    }

    @Provides
    fun getUploadFileService(retrofit: Retrofit): UploadFileService {
        
        
        
        return retrofit.create(UploadFileService::class.java)
    }
}
