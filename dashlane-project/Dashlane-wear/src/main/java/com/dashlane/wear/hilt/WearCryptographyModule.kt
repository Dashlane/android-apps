package com.dashlane.wear.hilt

import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.usercryptography.UserCryptographyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
fun interface WearCryptographyModule {
    @Binds
    fun bindUserCryptographyRepository(impl: UserCryptographyRepositoryImpl): UserCryptographyRepository
}