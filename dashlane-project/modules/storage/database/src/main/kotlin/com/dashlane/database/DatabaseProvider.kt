package com.dashlane.database

import com.dashlane.cryptography.CryptographyKey

interface DatabaseProvider {
    @Throws(
        DatabaseCryptographyException::class,
        DatabaseIndexNotFoundException::class,
        DatabaseIndexInvalidException::class,
        DatabaseIndexUserNotMatchException::class,
        DatabaseIndexInitializationException::class,
    )
    suspend fun open(
        userName: String,
        key: CryptographyKey.Raw32,
    ): Database

    fun getDatabase(userName: String): Database?

    fun closeDatabase(userName: String)

    suspend fun exist(userName: String): Boolean

    suspend fun delete(userName: String): Boolean

    suspend fun rename(userName: String, newUserName: String, key: CryptographyKey.Raw32): Boolean

    suspend fun isAccessible(
        userName: String,
        key: CryptographyKey.Raw32,
    ): Boolean
}