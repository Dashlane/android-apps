package com.dashlane.wear.hilt

import android.app.backup.BackupManager
import android.content.Context
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class WearApplicationContextModule {
    @Provides
    fun provideBackupManager(
        @ApplicationContext context: Context,
    ): BackupManager = BackupManager(context)

    @Provides
    fun providePackageManager(
        @ApplicationContext context: Context,
    ): PackageManager = context.packageManager
}