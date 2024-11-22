package com.dashlane.authentication.hilt

import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
fun interface AuthenticationLocalKeyModule {
    @Binds
    fun bindLocalKeyRepository(
        localKeyRepository: AuthenticationLocalKeyRepositoryImpl
    ): AuthenticationLocalKeyRepository
}