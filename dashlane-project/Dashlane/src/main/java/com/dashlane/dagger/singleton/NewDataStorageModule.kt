package com.dashlane.dagger.singleton

import android.content.Context
import com.dashlane.cryptography.Cryptography
import com.dashlane.database.DatabaseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NewDataStorageModule {
    @Provides
    @Singleton
    fun provideDatabaseProvider(
        @ApplicationContext context: Context,
        cryptography: Cryptography
    ): DatabaseProvider {
        return DatabaseProvider(context.filesDir, cryptography)
    }
}
