package com.dashlane.cryptography

interface CryptographyComponent {
    val cryptography: Cryptography
    val saltGenerator: SaltGenerator
    val keyGenerator: CryptographyKeyGenerator
}