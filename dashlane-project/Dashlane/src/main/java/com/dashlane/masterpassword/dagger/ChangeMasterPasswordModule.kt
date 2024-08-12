package com.dashlane.masterpassword.dagger

import com.dashlane.masterpassword.ChangeMasterPasswordContract
import com.dashlane.masterpassword.ChangeMasterPasswordDataProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ChangeMasterPasswordModule {
    @Binds
    fun bindChangeMasterPasswordDataProvider(
        dataProvider: ChangeMasterPasswordDataProvider?
    ): ChangeMasterPasswordContract.DataProvider?
}