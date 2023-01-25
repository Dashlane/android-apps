package com.dashlane.nitro

import com.dashlane.nitro.cryptography.NitroCryptography
import com.dashlane.nitro.cryptography.NitroCryptographyImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal abstract class NitroModule {

    @Binds
    abstract fun bindNitroCryptography(impl: NitroCryptographyImpl): NitroCryptography

    @Binds
    abstract fun bindAttestationValidator(impl: AttestationValidatorImpl): AttestationValidator

    @Binds
    abstract fun bindNitro(impl: NitroImpl): Nitro
}