package com.dashlane.storage.userdata.accessor.dagger

import com.dashlane.storage.userdata.accessor.DataSaverImpl
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.core.history.DataChangeHistoryQueryProviderImpl
import com.dashlane.core.history.DataChangeHistoryQueryProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessorImpl
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import dagger.Binds
import dagger.Module

@Module
interface UserDataAccessorModule {
    @Binds
    fun bindDataSaver(impl: DataSaverImpl): DataSaver

    @Binds
    fun bindDataChangeHistoryQueryProvider(impl: DataChangeHistoryQueryProviderImpl): DataChangeHistoryQueryProvider

    @Binds
    fun bindMainDataAccessor(impl: MainDataAccessorImpl): MainDataAccessor
}