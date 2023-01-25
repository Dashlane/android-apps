package com.dashlane.masterpassword.dagger

import com.dashlane.masterpassword.ChangeMasterPasswordContract
import com.dashlane.masterpassword.ChangeMasterPasswordDataProvider
import dagger.Binds
import dagger.Module

@Module
interface ChangeMasterPasswordModule {
    @Binds
    fun bindChangeMasterPasswordDataProvider(
        dataProvider: ChangeMasterPasswordDataProvider?
    ): ChangeMasterPasswordContract.DataProvider?
}