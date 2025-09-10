package com.dashlane.wear.hilt

import com.dashlane.lock.LockHelper
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockSelfChecker
import com.dashlane.lock.LockTimeManager
import com.dashlane.lock.LockTimeManagerImpl
import com.dashlane.lock.LockTypeManager
import com.dashlane.wear.lock.WearLockNavigationHelper
import com.dashlane.wear.lock.WearLockTypeManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WearLockModule {
    @Binds
    fun bindsLockTypeManager(impl: WearLockTypeManager): LockTypeManager

    @Binds
    fun bindsLockNavigationHelper(impl: WearLockNavigationHelper): LockNavigationHelper

    @Binds
    fun bindsLockTimeManger(impl: LockTimeManagerImpl): LockTimeManager

    @Binds
    fun bindsLockHelper(impl: LockManager): LockHelper

    @Binds
    fun bindsLockSelfChecker(impl: LockManager): LockSelfChecker
}