package com.dashlane.database

import java.io.File

internal interface TransactionRepository {
    val markerFile: File

    val markerExist: Boolean
        get() = markerFile.exists()

    fun releaseMarker()

    suspend fun withMarker(block: suspend () -> Unit)
}