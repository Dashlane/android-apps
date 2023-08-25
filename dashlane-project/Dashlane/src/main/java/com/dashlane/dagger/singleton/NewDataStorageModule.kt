package com.dashlane.dagger.singleton

import android.content.Context
import com.dashlane.database.DatabaseProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import com.dashlane.cryptography.Cryptography
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
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
