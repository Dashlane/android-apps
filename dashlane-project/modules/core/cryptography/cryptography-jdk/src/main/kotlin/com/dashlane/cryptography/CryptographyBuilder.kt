package com.dashlane.cryptography

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class CryptographyBuilder(
    var keyDerivationEngine: KeyDerivationEngine? = null,
    var cipherFactory: CipherFactory? = null,
    var macFactory: MacFactory? = null,
    var saltGenerator: SaltGenerator? = null,
    var ivGenerator: IvGenerator? = null,
    var paddingGenerator: PaddingGenerator? = null,
    var random: Random = SecureRandom().asKotlinRandom(),
) {
    fun build(): Cryptography {
        val keyDerivationEngine = keyDerivationEngine ?: KeyDerivationEngine()
        val cipherFactory = cipherFactory ?: CipherFactory()
        val macFactory = macFactory ?: MacFactory()
        val saltGenerator = saltGenerator ?: SaltGenerator(random)
        val ivGenerator = ivGenerator ?: IvGenerator(random)
        val paddingGenerator = paddingGenerator ?: PaddingGenerator(random)
        return Cryptography(
            cipherFactory,
            macFactory,
            keyDerivationEngine,
            saltGenerator,
            ivGenerator,
            paddingGenerator,
        )
    }
}

@Suppress("FunctionName")
inline fun Cryptography(builder: CryptographyBuilder.() -> Unit = {}): Cryptography =
    CryptographyBuilder().apply(builder).build()