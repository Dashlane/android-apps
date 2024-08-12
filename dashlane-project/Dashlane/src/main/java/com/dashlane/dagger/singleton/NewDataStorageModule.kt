package com.dashlane.dagger.singleton

import android.content.Context
import com.dashlane.cryptography.Cryptography
import com.dashlane.database.DatabaseProvider
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

@Module
@InstallIn(SingletonComponent::class)
object NewDataStorageModule {
    @Provides
    @Singleton
    fun provideDatabaseProvider(
        @ApplicationContext context: Context,
        @ApplicationCoroutineScope coroutineScope: CoroutineScope,
        @IoCoroutineDispatcher ioDispatcher: CoroutineDispatcher,
        cryptography: Cryptography
    ): DatabaseProvider {
        return DatabaseProvider(rootDirAsync = coroutineScope.async { context.filesDir }, cryptography = cryptography, ioDispatcher = ioDispatcher)
    }
}
