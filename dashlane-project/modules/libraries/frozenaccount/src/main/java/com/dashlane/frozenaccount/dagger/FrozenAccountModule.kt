package com.dashlane.frozenaccount.dagger

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.frozenaccount.FrozenStateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FrozenAccountModule {

    @Binds
    fun bindsFrozenStateManager(impl: FrozenStateManagerImpl): FrozenStateManager
}