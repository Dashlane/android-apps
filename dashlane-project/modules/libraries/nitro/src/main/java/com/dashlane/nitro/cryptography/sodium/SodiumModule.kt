package com.dashlane.nitro.cryptography.sodium

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal abstract class SodiumModule {
    @Binds
    abstract fun bindSodiumCryptography(impl: SodiumCryptographyImpl): SodiumCryptography
}