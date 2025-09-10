package com.dashlane.cryptography

interface CryptographySettings {
    fun getOrCreateMarker(keyType: CryptographyKey.Type): CryptographyMarker

    fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt
}