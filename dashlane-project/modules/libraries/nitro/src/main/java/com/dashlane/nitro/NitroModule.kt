package com.dashlane.nitro

import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.NitroSecretStreamClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal abstract class NitroModule {

    @Binds
    abstract fun bindNitroSecretStreamClient(impl: NitroSecretStreamClientImpl): NitroSecretStreamClient

    @Binds
    abstract fun bindAttestationValidator(impl: AttestationValidatorImpl): AttestationValidator

    @Binds
    abstract fun bindNitro(impl: NitroImpl): Nitro
}