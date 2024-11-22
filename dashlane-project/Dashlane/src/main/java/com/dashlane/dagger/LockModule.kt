package com.dashlane.dagger

import com.dashlane.lock.LockManager
import com.dashlane.lock.LockSelfChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
fun interface LockModule {
    @Binds
    fun bindsLockSelfChecker(impl: LockManager): LockSelfChecker
}