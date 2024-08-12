package com.dashlane.dagger.singleton

import com.dashlane.core.KeyChainHelper
import com.dashlane.core.KeyChainHelperImpl
import com.dashlane.cryptography.CipherFactory
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKeyGenerator
import com.dashlane.cryptography.IvGenerator
import com.dashlane.cryptography.KeyDerivationEngine
import com.dashlane.cryptography.LegacyAppDecryptionProvider
import com.dashlane.cryptography.MacFactory
import com.dashlane.cryptography.PaddingGenerator
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.cryptography.jni.JniCryptography
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.security.SecureRandom
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.random.asKotlinRandom

@Module
@InstallIn(SingletonComponent::class)
object CryptographyModule {
    @Provides
    @Singleton
    fun provideJniCryptography(): JniCryptography = JniCryptography()

    @Provides
    fun provideCryptography(
        cipherFactory: CipherFactory,
        macFactory: MacFactory,
        keyDerivationEngine: KeyDerivationEngine,
        saltGenerator: SaltGenerator,
        ivGenerator: IvGenerator,
        paddingGenerator: PaddingGenerator
    ): Cryptography = Cryptography(
        cipherFactory,
        macFactory,
        keyDerivationEngine,
        saltGenerator,
        ivGenerator,
        paddingGenerator
    )

    @Provides
    fun provideMacFactory(): MacFactory = MacFactory()

    @Provides
    fun provideCipherFactory(): CipherFactory = CipherFactory()

    @Provides
    fun provideKeyDerivationEngine(jniCryptography: JniCryptography): KeyDerivationEngine =
        KeyDerivationEngine(jniCryptography)

    @Provides
    fun providePaddingGenerator(random: Random): PaddingGenerator = PaddingGenerator(random)

    @Provides
    fun provideIvGenerator(random: Random): IvGenerator = IvGenerator(random)

    @Provides
    fun provideSaltGenerator(random: Random): SaltGenerator = SaltGenerator(random)

    @Provides
    fun provideKeyGenerator(random: Random): CryptographyKeyGenerator =
        CryptographyKeyGenerator(random)

    @Provides
    fun provideRandom(): Random = SecureRandom().asKotlinRandom()

    @Provides
    fun provideAppDecryptionProvider(
        cryptography: Cryptography,
        jniCryptography: JniCryptography
    ): LegacyAppDecryptionProvider = LegacyAppDecryptionProvider(cryptography, jniCryptography)

    @Provides
    fun provideSharingCryptography(jniCryptography: JniCryptography): SharingCryptography =
        SharingCryptography(jniCryptography)

    @Provides
    fun provideKeyChainHelper(impl: KeyChainHelperImpl): KeyChainHelper = impl
}