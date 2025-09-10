package com.dashlane.attachment.dagger

import com.dashlane.securefile.DownloadFileContract
import com.dashlane.securefile.DownloadFileDataProvider
import com.dashlane.securefile.UploadFileContract
import com.dashlane.securefile.UploadFileDataProvider
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.securefile.storage.SecureFileStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SecureFileBindingModule {
    @Binds
    fun bindUploadFileProvider(uploadFileDataProvider: UploadFileDataProvider): UploadFileContract.DataProvider

    @Binds
    fun bindDownloadFileProvider(downloadFileDataProvider: DownloadFileDataProvider): DownloadFileContract.DataProvider

    @Binds
    fun bindSecureFileStorage(impl: SecureFileStorageImpl): SecureFileStorage
}