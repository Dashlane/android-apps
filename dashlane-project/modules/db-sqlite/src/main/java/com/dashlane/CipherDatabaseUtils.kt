package com.dashlane

import com.dashlane.util.MD5Hash

object CipherDatabaseUtils {
    private const val DATABASE_EXTENSION = ".aes"
    @JvmStatic
    fun getDatabaseName(userId: String) = MD5Hash.hash(userId) + DATABASE_EXTENSION
}