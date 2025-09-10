package com.dashlane.wear.hilt

import android.content.Context
import com.dashlane.cryptography.Cryptography
import com.dashlane.database.DatabaseProvider
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.DatabaseItemSaverRaclette
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.CredentialDataQueryImpl
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.DataCounterImpl
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQueryImpl
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQueryImpl
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.wear.credentialmanager.WearDataSaver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface WearDataStorageModule {
    @Binds
    fun bindsDatabaseItemSaver(impl: DatabaseItemSaverRaclette): DatabaseItemSaver

    @Binds
    fun bindsVaultDataQuery(impl: VaultDataQueryImpl): VaultDataQuery

    @Binds
    fun bindsGenericDataQuery(impl: GenericDataQueryImpl): GenericDataQuery

    @Binds
    fun bindCredentialDataQuery(impl: CredentialDataQueryImpl): CredentialDataQuery

    @Binds
    fun bindsDataSaver(impl: WearDataSaver): DataSaver

    @Binds
    fun bindsDataCounter(impl: DataCounterImpl): DataCounter

    companion object {
        @Provides
        @Singleton
        fun provideDatabaseProvider(
            @ApplicationContext context: Context,
            @ApplicationCoroutineScope coroutineScope: CoroutineScope,
            @IoCoroutineDispatcher ioDispatcher: CoroutineDispatcher,
            cryptography: Cryptography,
        ): DatabaseProvider {
            return DatabaseProvider(
                rootDirAsync = coroutineScope.async { context.filesDir },
                cryptography = cryptography,
                ioDispatcher = ioDispatcher,
            )
        }
    }
}